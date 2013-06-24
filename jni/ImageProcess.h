#include <opencv2/opencv.hpp>
#include <highgui.h>
#include <math.h>
#include <string>
#include "debugOut.h"
//图片处理类
class ImageProcess{
    public:
    //缩放图片，指定源图片及目标宽高
    bool reSizeImage(std::string,int,int);
};
