#/*
# * Androusb
# * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
# * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
# * License details are in the file license.txt, distributed as part of this software.
# */

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := common_static

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/../ \
	$(LOCAL_PATH)/ \

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_C_INCLUDES)

LOCAL_SRC_FILES := \
	common_utils.cpp \
	JNIHelp.cpp \
	JniConstants.cpp \
	Timers.cpp \

LOCAL_CFLAGS := $(LOCAL_C_INCLUDES:%=-I%)
#マクロ定義
LOCAL_CFLAGS += -DANDROID_NDK
LOCAL_CFLAGS += -DNDEBUG							# LOG_ALLを無効にする・assertを無効にする場合
LOCAL_CFLAGS += -DLOG_NDEBUG						# デバッグメッセージを出さないようにする時
#LOCAL_CFLAGS += -DUSE_LOGALL						# define USE_LOGALL macro to enable all debug string
#LOCAL_CFLAGS += -DDISABLE_IMPORTGL					# when static link OpenGL|ES library
#
#LOCAL_CPPFLAGS += -fexceptions						# 例外を有効にする
#LOCAL_CPP_FEATURES += exceptions 
LOCAL_CPPFLAGS += -frtti							# RTTI(実行時型情報)を有効にする
LOCAL_CFLAGS += -Wno-multichar

#public関数のみエクスポートする
LOCAL_CFLAGS += -Wl,--version-script,common.map

#最適化設定
LOCAL_CFLAGS += -DAVOID_TABLES
LOCAL_CFLAGS += -O3 -fstrict-aliasing
LOCAL_CFLAGS += -fprefetch-loop-arrays

LOCAL_EXPORT_LDLIBS := -L$(SYSROOT)/usr/lib -ldl	# to avoid NDK issue(no need for static library)
LOCAL_EXPORT_LDLIBS += -llog						# log output library

LOCAL_ARM_MODE := arm

include $(BUILD_STATIC_LIBRARY)

######################################################################
# libcommon.so
######################################################################
include $(CLEAR_VARS)
LOCAL_EXPORT_LDLIBS += -llog
LOCAL_EXPORT_C_INCLUDES := \
	$(LOCAL_PATH)/ \

LOCAL_WHOLE_STATIC_LIBRARIES = common_static

LOCAL_MODULE := libcommon
include $(BUILD_SHARED_LIBRARY)
