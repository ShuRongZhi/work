#include "FileHelper.h"
#include <sys/types.h>
#include <dirent.h>
#include <sys/stat.h>
#include <unistd.h>
#include <stdio.h>
#include <cstdlib>

//判断存放原图的缓存文件夹是否存在
bool FileHelper::checkOriginImageCacheExits()
{
    return this->checkFolderExits(OriginCachePath);
}
//判断存放缩小后图片的缓存文件夹是否存在
bool FileHelper::checkSmallImageCacheExits()
{
    return this->checkFolderExits(SmallCachePath);
}
//创建原图缓存文件夹
void FileHelper::createOriginCacheFolder()
{
    this->createFolder(OriginCachePath);

}
//创建小图缓存文件夹
void FileHelper::createSmallCacheFolder()
{
    this->createFolder(SmallCachePath);
}
//删除图片，传递all表示清空缓存
bool FileHelper::delImage(std::string path)
{
    if(0 == path.length())
	{
		LOGE("delImage不接受空参数!");
		return false;
	}
	if("all" == path)
	{
	    std::string command;
	    command = "rm -r " + OriginCachePath;
	    LOGD("删除原图缓存文件夹：%s",command.c_str());
		system(command.c_str());
	    command = "rm -r " + SmallCachePath;
	    LOGD("删除小图缓存文件夹:%s",command.c_str());
		system(command.c_str());
		return true;
	}
	else
	{
		LOGD("尝试删除文件:%s",path.c_str());
		if(-1 == remove(path.c_str()))
		{
		    LOGD("删除图片失败");
			return false;
		}
		else
		{
		    LOGD("删除图片成功");
			return true;
		}
	}
}
//检查原图是否存在，传递图片名
bool FileHelper::checkOriginImageExits(std::string fileName)
{
    std::string name = OriginCachePath + fileName;
    return checkFileExits(name);
}
//检查小图是否存在，传递图片名
bool FileHelper::checkSmallImageExits(std::string fileName)
{
     std::string name = SmallCachePath + fileName;
    return checkFileExits(name);
}
//检查文件是否存在，传递完整路径
bool FileHelper::checkFileExits(std::string filename)
{
    _file.open(filename.c_str(),std::ios::in);
    if(!_file)
    {
        //LOGD("该文件不存在");
        return false;
    }
    else
    {
        //LOGD("该文件已存在");
        _file.close();
        return true;
    }
}
//取得原图缓存文件夹路径
std::string FileHelper::getOriginCachePath()
{
    return OriginCachePath;
}
//取得小图缓存文件夹路径
std::string FileHelper::getSmallCachePath()
{
    return SmallCachePath;
}
//判断文件夹是否存在
bool FileHelper::checkFolderExits(std::string folderName)
{
    if(0 == access(folderName.c_str(),F_OK))
    {
        return true;
    }
    else
    {
        return false;
    }
}
//创建目录
bool FileHelper::createFolder(std::string folderName)
{
    if(0 == mkdir(folderName.c_str(), 0777))
    {
        return true;
    }
    else
    {
        return false;
    }
}
