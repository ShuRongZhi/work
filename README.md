work
====
研究ListView缓存及Jni使用
此应用会从网络下载图片，并显示到ListView上
图片的下载和内存缓存都由Native层负责
Java层负责从Native层读取内存数据，生成Bitmap并显示到ListView上
2013-6-26更新：
现在支持在Native层对图像进行缩放，只不过OpenCv的cvloadImage函数耗时较长导致图片缩放时间在1秒多，待解决中。
