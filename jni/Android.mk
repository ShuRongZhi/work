LOCAL_PATH := $(call my-dir)

CFLAGS := -Wpointer-arith -Wwrite-strings -Wunused -Winline \
 -Wnested-externs -Wmissing-declarations -Wmissing-prototypes -Wno-long-long \
 -Wfloat-equal -Wno-multichar -Wsign-compare -Wno-format-nonliteral \
 -Wendif-labels -Wstrict-prototypes -Wdeclaration-after-statement \
 -Wno-system-headers -DHAVE_CONFIG_H

include $(CLEAR_VARS)
include $(LOCAL_PATH)/curl/lib/Makefile.inc
include $(LOCAL_PATH)/OpenCV/jni/OpenCV.mk

OPENCV_INSTALL_MODULES:=on
OPENCV_CAMERA_MODULES:=off
OPENCV_LIB_TYPE:=STATIC

LOCAL_SRC_FILES := $(addprefix curl/lib/,$(CSOURCES))

LOCAL_CFLAGS += $(CFLAGS)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/curl/include/ $(LOCAL_PATH)/curl/lib
#添加opencv的头文件，不然会报找不到头文件的错误，注意include后面有/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/OpenCV/jni/include/


LOCAL_COPY_HEADERS_TO := libcurl
LOCAL_COPY_HEADERS := $(addprefix curl/include/curl/,$(HHEADERS))

LOCAL_MODULE:= libcurl

include $(BUILD_STATIC_LIBRARY)


#清除变量了，所以还得再添加
include $(CLEAR_VARS)

LOCAL_MODULE    := imageHelper
LOCAL_SRC_FILES := com_dirs_work_JniHelper.cpp | CacheManager.cpp | TimeHelper.cpp | FileHelper.cpp | DownloadHelper.cpp | ImageProcess.cpp
LOCAL_STATIC_LIBRARIES := libcurl
LOCAL_C_INCLUDES += $(LOCAL_PATH)/curl/include $(LOCAL_PATH)/curl/lib
#添加opencv的头文件，不然会报找不到头文件的错误，注意include后面没有/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/OpenCV/jni/include
LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog

include $(BUILD_SHARED_LIBRARY)

