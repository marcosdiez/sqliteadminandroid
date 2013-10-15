LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := filter
LOCAL_SRC_FILES := filter.c

include $(BUILD_SHARED_LIBRARY)

TARGET_ARCH := arm