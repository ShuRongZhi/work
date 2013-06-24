#include "DownloadHelper.h"
#include "FileHelper.h"
#include "ImageProcess.h"


std::string CachePath;
//设置下载结构体
struct DownInfo{
  //文件名
  std::string _ImageID;
  //文件输出
  FILE * _stream;
}_downInfo;


//回调函数,用于获取数据
static size_t WirteToFile(void *buffer, size_t size, size_t nmemb, void *stream)
{
  LOGD("缓冲区大小: %d",size*nmemb);
  struct DownInfo *out = (struct DownInfo *)stream;
  if(out && !out->_stream)
  {
    //以  漫画ID_图片ID  的格式作为文件名
    std::string file_name = CachePath + out->_ImageID;
    //以二进制的方法打开文件
    out->_stream=fopen(file_name.c_str(), "wb+");
    if(!out->_stream)
    {        
        LOGE("无法打开目标文件写入! 文件名:%s",file_name.c_str());
        return -1;
    }
  }
  size_t res = fwrite(buffer, size, nmemb, out->_stream);
  return res;
} 


std::string DownloadHelper::getImage(std::string ImageID,bool isZoom)
{
    //图像处理类
    ImageProcess mImageProcess;
    //文件助手类
    FileHelper l_mFileHelper;
    //取得存放原图的缓存路径
    CachePath = l_mFileHelper.getOriginCachePath();
    if(isZoom)
    {
        if(l_mFileHelper.checkSmallImageExits(ImageID))
        {
            LOGD("小图已缓存，返回小图");
            return l_mFileHelper.getSmallCachePath() + ImageID;
        }
        else
        {
            LOGD("小图未缓存，查询大图是否存在");
            if(l_mFileHelper.checkOriginImageExits(ImageID))
            {
                LOGD("大图已缓存，转换成小图");
                if(!mImageProcess.reSizeImage(ImageID,300,300))
                {
                    LOGE("转换失败!");
                    return "error";
                }
                else
                {
                    LOGD("转换成功，返回小图");
                    return l_mFileHelper.getSmallCachePath() + ImageID;
                }
            }
            else
            {
                LOGD("图片未缓存，向网络请求图片");
                //给结构体设置成员变量
                _downInfo._ImageID = ImageID;
                std::string url;
                //生成获取图片url
                url = "http://tonjay123.webcrow.jp/Image/"+ ImageID;
                //判断下载是否成功
                if(download(url))
                {
                    LOGD("图片下载完毕，转换成小图");
                    if(!mImageProcess.reSizeImage(ImageID,300,300))
                    {
                        LOGE("转换失败!");
                        return "error";
                    }
                    else
                    {
                        return l_mFileHelper.getSmallCachePath() + ImageID;
                    }
                }
                else
                {
                     //下载失败，删除失败文件
                     l_mFileHelper.delImage(l_mFileHelper.getSmallCachePath() + ImageID);
                     return "error";
                }
            }
        }
    }
    else
    {
        if(l_mFileHelper.checkOriginImageExits(ImageID))
        {
            LOGD("图片已缓存，返回图片");
            return l_mFileHelper.getOriginCachePath() + ImageID;
        }
        else
        {
            LOGD("图片未缓存，向网络请求图片");
            //给结构体设置成员变量
            _downInfo._ImageID = ImageID;
            std::string url;
            //生成获取图片url
            url = "http://tonjay123.webcrow.jp/Image/"+ ImageID;
            //判断下载是否成功
            if(download(url))
            {  
                return l_mFileHelper.getOriginCachePath() + ImageID;
            }
            else
            {
                //下载失败，删除失败文件
                l_mFileHelper.delImage(l_mFileHelper.getOriginCachePath() + ImageID);
                return "error";
            }
        }
    }
}

bool DownloadHelper::download(std::string url)
{
    bool returnValue = true;
    if(0 == url.length())
    {
        LOGE("请求资源的地址为空");
        return false;
    }
    else
    {
        LOGD("请求的URL地址为:%s",url.c_str());   
        //初始化 curl_global_init
        curl_global_init(CURL_GLOBAL_DEFAULT);
        curl = curl_easy_init(); 
        if(curl) {
            curl_easy_setopt(curl, CURLOPT_URL,url.c_str()); // 设置访问链接
            curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION,WirteToFile); // 设置回调函数
            curl_easy_setopt(curl, CURLOPT_WRITEDATA, &_downInfo); //设置回调参数（在回调函数内可以调用)
            res = curl_easy_perform(curl); //执行获得curl返回参数用于判断成功与否
            curl_easy_cleanup(curl);
            
            if(CURLE_OK == res)
            {
                returnValue = true;   
            }
            else
            {
                LOGE("下载失败！");
                setError(-1);
                returnValue = false;
            }
            
        }
     
        if(_downInfo._stream)
        {
            //关闭文件
            if(0 != fclose(_downInfo._stream))
            {
                LOGE("关闭文件失败!");
            }
            _downInfo._stream = NULL;
        }
        
        LOGD("清空资源");
        curl_global_cleanup();
    }
    return returnValue;
}

//设置错误码，赋值给成员变量errCode
void DownloadHelper::setError(int err)
{
    errCode = err;
}

//取得成员变量errCode的值返回
int DownloadHelper::getError()
{
    return errCode;
}

