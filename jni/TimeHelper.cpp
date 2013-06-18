#include "TimeHelper.h"

int TimeHelper::setStart()
{
   reset();
    int ret = 0;
    ret = gettimeofday(&pstart,NULL);
	if (ret != 0) {
        return -1;
	}
    return 0;
}

int TimeHelper::setEnd()
{
    int ret = 0;
    ret = gettimeofday(&pend,NULL);
	if (ret != 0) {
        return -1;
	}
    return 0;
}

float TimeHelper::getUseTime()
{
    float time_used;
	time_used = 1000000*(pend.tv_sec - pstart.tv_sec)+ pend.tv_usec - pstart.tv_usec;
	time_used /= 1000000;
	reset();
	return time_used;
}

void TimeHelper::reset()
{
    
    pend.tv_sec = 0;
    pstart.tv_sec = 0;
    pend.tv_usec = 0;
    pstart.tv_usec = 0;
    
}
