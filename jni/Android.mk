LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
OPENCV_LIB_TYPE:=STATIC
include /home/jay/Android_OpenCV/OpenCV-2.4.4-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := disp_img
LOCAL_SRC_FILES := jni_part.cpp
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)
