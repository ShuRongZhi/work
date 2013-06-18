#include <fstream>
#include <string>
#include "debugOut.h"

class FileHelper{
    public:
        
        FileHelper(){
            //LOGD("Native层:  FileHelper构造函数");
            CacheName = "/mnt/sdcard/.ImageCache/";
        }
        ~FileHelper(){
            //LOGD("Native层:  FileHelper析构函数");
        }
        //判断文件是否存在
        bool checkFileExits(std::string);
        //判断文件夹是否存在
        bool checkFolderExits(std::string);
        //判断缓存文件夹是否存在
        bool checkCacheFolderExits();
        //创建目录
        bool createFolder(std::string);
        //创建缓存文件夹
        bool createCacheFolder();
        //取得图片路径
        std::string getImagePath(std::string);
        //取得缓存目录路径
        std::string getCachePath();
        //删除指定图片，传递all表示清空cache
        bool delImage(std::string);
    private:
        std::ofstream _file;
        //缓存文件夹名称
        std::string CacheName;
};
