LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_CFLAGS += -std=c99 -g
LOCAL_MODULE    := plasma
LOCAL_SRC_FILES := util.c plasma.c starfield.c fire.c
LOCAL_LDLIBS    := -g -lm -llog -ljnigraphics

include $(BUILD_SHARED_LIBRARY)
