#include "ImageProcess.h"
#include "FileHelper.h"
#include "TimeHelper.h"
//缩放图片，指定源图位置，目标图像宽高，成功返回true，否则返回false
bool ImageProcess::reSizeImage(std::string image,int width,int height)
{
    //计时器
    TimeHelper mTime;
    using namespace cv;
    FileHelper mFile;
    
    IplImage *src = NULL;
    IplImage *dst = NULL;
    CvSize cvsize;
    //设置完整路径
    std::string path = mFile.getOriginCachePath() + image;
    //载入图像
    mTime.setStart();
    src = cvLoadImage(path.c_str());
    mTime.setEnd();
    LOGD("载入图片耗时:%f",mTime.getUseTime());
    if(src != NULL)
    {
        //设置宽高
        cvsize.width = width;
        cvsize.height = height;
        
        //创建目标图像
        mTime.setStart();
        dst = cvCreateImage(cvsize,src->depth,src->nChannels);
        mTime.setEnd();
        LOGD("创建图像耗时:%f",mTime.getUseTime());
        
        //将原图缩放到目标图像大小并复制到目标图像中
        mTime.setStart();
        cvResize(src, dst, CV_INTER_LINEAR);
        mTime.setEnd();
        LOGD("缩放图片耗时:%f",mTime.getUseTime());
        
        //释放源图占用的内存
        cvReleaseImage(&src);
        src = NULL;
        //保存目标图像
        std::string name = mFile.getSmallCachePath() + image; 
        if(cvSaveImage(name.c_str(),dst))
        {
            LOGD("保存图像成功");
        }
        else
        {
        
            LOGE("保存图像失败");
            //释放目标图像占用的内存
            cvReleaseImage(&dst);
            dst = NULL;
            
            return false;
        }
        //释放目标图像占用的内存
        cvReleaseImage(&dst);
        dst = NULL;
        return true;
    }
    else
    {
        LOGE("载入图像失败:%s",path.c_str());
        return false;
    }
    
}
