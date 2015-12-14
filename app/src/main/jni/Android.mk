LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := imageeffects
LOCAL_SRC_FILES := effects.c

include $(BUILD_SHARED_LIBRARY)