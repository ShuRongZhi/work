#include "com_dirs_work_JniHelper.h"
#include "DownloadHelper.h"
#include "CacheManager.h"

#include "ImageProcess.h"

//声明函数原型
std::string JstringToString(jstring);
//全局变量
JNIEnv *g_env;
CacheManager mCache;
bool isZoom;
std::string mOriginCache; 
std::string mSmallCache;
//计时器
TimeHelper mTime;
//图像处理类
ImageProcess mImageProcess;

void Java_com_dirs_work_JniHelper_init(JNIEnv *env, jobject thisz, jboolean flag)
{

    LOGD("初始化Native");
    FileHelper mFileHelper;
    isZoom = flag;
    g_env = env;
    if(isZoom)
    {
        LOGD("在Native层处理图像缩放");
    }
    if(!mFileHelper.checkSmallImageCacheExits() || !mFileHelper.checkOriginImageCacheExits())
    {
        LOGD("缓存文件夹不存在，创建！");
        mFileHelper.createOriginCacheFolder();
        mFileHelper.createSmallCacheFolder();
    }
    mOriginCache = mFileHelper.getOriginCachePath(); 
    mSmallCache = mFileHelper.getSmallCachePath();
    if(!mFileHelper.checkFolderExits(mOriginCache + "1MP"))
    {
        mFileHelper.createFolder(mOriginCache + "1MP");
    }
    if(!mFileHelper.checkFolderExits(mOriginCache + "2MP"))
    {
        mFileHelper.createFolder(mOriginCache + "2MP");
    }
    if(!mFileHelper.checkFolderExits(mSmallCache + "1MP"))
    {
        mFileHelper.createFolder(mSmallCache + "1MP");
    }
    if(!mFileHelper.checkFolderExits(mSmallCache + "2MP"))
    {
        mFileHelper.createFolder(mSmallCache + "2MP");
    }
}

//根据图片路径取得byte[]
jbyteArray Java_com_dirs_work_JniHelper_getImage(JNIEnv *env, jobject thisz, jstring imageID)
{
	//将jstring转换成std::String
    std::string _imageID = JstringToString(imageID);
    if(0 == _imageID.length())
    {
        LOGD("错误:必须指定图片ID!");
        return 0;
    }
    
    //图片下载助手类
    DownloadHelper mDownHelper;
    
    
    if("error" == mDownHelper.getImage(_imageID,isZoom))
    {
        return 0;
    }
    else
    {
        if(isZoom)
        {
           _imageID = mSmallCache + _imageID;
        }
        else
        {
            _imageID = mOriginCache + _imageID;
        }

        //定义指针p，并用来接受CacheMager::getMemory返回的char *
        char * p;
        p = mCache.getMemory(_imageID);
        if(NULL == p)
        {
            LOGD("指针为空!");
            return 0;
        }
        else
        {
        	//取得图片长度
            int length = mCache.getImageSize(_imageID);
            if(-1 == length)
            {
                LOGE("长度为空!");
                return 0;
            }
            else
            {
                mTime.setStart();
                //将char*转换成jbyte*
                jbyte *jby = (jbyte*)p;
                //创建一个jbyteArray数组
                jbyteArray jarray = env->NewByteArray(length);
                //将char里的数据复制到jarray中
                env->SetByteArrayRegion(jarray,0,length,jby);
                mTime.setEnd();
                LOGD("将char*转换成ByreArray耗时:%f",mTime.getUseTime());
                return jarray;
            }
        }
    
    }
}



//将jstring转换为std::string
std::string JstringToString(jstring source)
{
    const char* str;
    str = g_env->GetStringUTFChars(source,false);
    std::string temp(str);
    g_env->ReleaseStringUTFChars(source,str);
    return temp;
}
