#include <fstream>
#include <string>
#include "debugOut.h"

class FileHelper{
    public:
        FileHelper(){
            OriginCachePath  = "/mnt/sdcard/.ImageCache/Origin/";
            SmallCachePath = "/mnt/sdcard/.ImageCache/Small/";
            if(!checkFolderExits("/mnt/sdcard/.ImageCache/"))
            {
                createFolder("/mnt/sdcard/.ImageCache/");
            }
        }
        //判断存放原图的缓存文件夹是否存在
        bool checkOriginImageCacheExits();
        //判断存放缩小后图片的缓存文件夹是否存在
        bool checkSmallImageCacheExits();
        //判断文件夹是否存在
        bool checkFolderExits(std::string);
        //创建文件夹，需要完整路径
        bool createFolder(std::string);
        //创建原图缓存文件夹
        void createOriginCacheFolder();
        //创建小图缓存文件夹
        void createSmallCacheFolder();
        //删除图片，传递all表示清空缓存
        bool delImage(std::string);
        //检查原图是否存在，传递图片名
        bool checkOriginImageExits(std::string);
        //检查小图是否存在，传递图片名
        bool checkSmallImageExits(std::string);
        //检查文件是否存在，传递完整路径
        bool checkFileExits(std::string);
        //取得原图缓存文件夹路径
        std::string getOriginCachePath();
        //取得小图缓存文件夹路径
        std::string getSmallCachePath();
        
     private:
        std::ofstream _file;
        //缓存文件夹名称
        std::string OriginCachePath;
        //缩小后图片文件夹的名称
        std::string SmallCachePath;
        
};
