#include <sys/time.h>
#include <unistd.h>

class TimeHelper{
    public:
        //设置开始时间
        int setStart();
        //设置结束时间
        int setEnd();
        //取得时间差
        float getUseTime();
        //复位时间
        void reset();
    private:
        struct timeval pstart;
        struct timeval pend;
};
