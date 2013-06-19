#include "com_dirs_work_JniHelper.h"
#include "DownloadHelper.h"
#include "CacheManager.h"
//声明函数原型
std::string JstringToString(jstring);
//全局变量
JNIEnv *g_env;
CacheManager mCache;
std::string mCacheName;
//计时器
TimeHelper mTime;


void Java_com_dirs_work_JniHelper_init(JNIEnv *env, jobject thisz)
{

    LOGD("初始化Native");
    FileHelper mFileHelper;
    if(!mFileHelper.checkCacheFolderExits())
    {
        LOGD("缓存文件夹不存在，创建！");
        if(!mFileHelper.createCacheFolder())
        {
            LOGE("致命错误，创建缓存文件夹失败!");
        }
    }
    mCacheName = mFileHelper.getCachePath();
    if(!mFileHelper.checkFolderExits(mCacheName + "1MP"))
    {
        mFileHelper.createFolder(mCacheName + "1MP");
    }
    if(!mFileHelper.checkFolderExits(mCacheName + "2MP"))
    {
        mFileHelper.createFolder(mCacheName + "2MP");
    }
    if(!mFileHelper.checkFolderExits(mCacheName + "3MP"))
    {
        mFileHelper.createFolder(mCacheName + "3MP");
    }
    mTime.setStart();
    g_env = env;
    mTime.setEnd();
    LOGD("初始化Native耗时:%f",mTime.getUseTime());
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

    if("error" == mDownHelper.getImage(_imageID))
    {
        LOGE("下载图片失败!");
        return 0;
    }
    else
    {
        _imageID = mCacheName + _imageID;
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
    str = g_env->GetStringUTFChars(source, false);
    std::string temp(str);
    g_env->ReleaseStringUTFChars(source,str);
    return temp;
}


jboolean Java_com_dirs_work_JniHelper_downloadImage(JNIEnv *env, jobject thisz, jstring image)
{
    const char* str;
    str = g_env->GetStringUTFChars(image, false);
    std::string _image(str);
    g_env->ReleaseStringUTFChars(image,str);
    
    if(0 == _image.length())
    {
        LOGD("Error,Native层downloadImage不接受空参数!");
        return false;
    }
    else
    {
        DownloadHelper mDownloadHelper;
        if("" == mDownloadHelper.getImage(_image))
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    
}
