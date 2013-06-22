APP_PROJECT_PATH := $(call my-dir)/..
APP_MODULES := imageHelper libcurl
#APP_STL := stlport_static
APP_STL := gnustl_static
APP_CPPFLAGS := -frtti -fexceptions
APP_ABI := armeabi-v7a armeabi
