LOCAL_PATH := $(call my-dir)

################################################################################
# OpenCV JNI interface library
# JavaバインディングだけからOpenCVを呼び出すのなら下のを有効にする
################################################################################
#include $(CLEAR_VARS)

#OPENCV_CAMERA_MODULES:=off
#OPENCV_INSTALL_MODULES:=on
#OPENCV_LIB_TYPE:=SHARED
#include $(LOCAL_PATH)/../opencv3/OpenCV.mk
#LOCAL_MODULE := libopencv_java3
#LOCAL_SRC_FILES := externalLibs/$(TARGET_ARCH_ABI)/$(LOCAL_MODULE)$(TARGET_SONAME_EXTENSION)
#include $(PREBUILT_SHARED_LIBRARY)
