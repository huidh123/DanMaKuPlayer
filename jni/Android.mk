LOCAL_PATH := $(call my-dir)

###########################
#
# SDL shared library
#
###########################

include $(CLEAR_VARS)

LOCAL_MODULE := SDL2

LOCAL_C_INCLUDES := $(LOCAL_PATH)/srcSDL/include

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_C_INCLUDES)

LOCAL_SRC_FILES := \
	$(subst $(LOCAL_PATH)/,, \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/audio/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/audio/android/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/audio/dummy/*.c) \
	$(LOCAL_PATH)/srcSDL/src/atomic/SDL_atomic.c \
	$(LOCAL_PATH)/srcSDL/src/atomic/SDL_spinlock.c.arm \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/core/android/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/cpuinfo/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/dynapi/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/events/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/file/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/haptic/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/haptic/dummy/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/joystick/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/joystick/android/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/loadso/dlopen/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/power/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/power/android/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/filesystem/dummy/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/render/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/render/*/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/stdlib/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/thread/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/thread/pthread/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/timer/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/timer/unix/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/video/*.c) \
	$(wildcard $(LOCAL_PATH)/srcSDL/src/video/android/*.c) \
    $(wildcard $(LOCAL_PATH)/srcSDL/src/test/*.c))

LOCAL_CFLAGS += -DGL_GLEXT_PROTOTYPES
LOCAL_LDLIBS := -ldl -lGLESv1_CM -lGLESv2 -llog -landroid

include $(BUILD_SHARED_LIBRARY)

###########################
#
# SDL static library
#
###########################

LOCAL_MODULE := SDL2_static

LOCAL_MODULE_FILENAME := libSDL2

LOCAL_SRC_FILES += $(LOCAL_PATH)/src/main/android/SDL_android_main.c

LOCAL_LDLIBS := 
LOCAL_EXPORT_LDLIBS := -Wl,--undefined=Java_org_libsdl_app_SDLActivity_nativeInit -ldl -lGLESv1_CM -lGLESv2 -llog -landroid

include $(BUILD_STATIC_LIBRARY)


#****************************************
#
#编译FFmpeg的android。mk
#
#
#****************************************
include $(CLEAR_VARS)  
LOCAL_MODULE :=avcodec-55-prebuilt  
LOCAL_SRC_FILES :=srcFFmpeg/prebuilt/libavcodec-55.so  
include $(PREBUILT_SHARED_LIBRARY)  
   
include $(CLEAR_VARS)  
LOCAL_MODULE :=avdevice-55-prebuilt  
LOCAL_SRC_FILES :=srcFFmpeg/prebuilt/libavdevice-55.so  
include $(PREBUILT_SHARED_LIBRARY)  
   
include $(CLEAR_VARS)  
LOCAL_MODULE :=avfilter-4-prebuilt  
LOCAL_SRC_FILES :=srcFFmpeg/prebuilt/libavfilter-4.so  
include $(PREBUILT_SHARED_LIBRARY)  
   
include $(CLEAR_VARS)  
LOCAL_MODULE :=avformat-55-prebuilt  
LOCAL_SRC_FILES :=srcFFmpeg/prebuilt/libavformat-55.so  
include $(PREBUILT_SHARED_LIBRARY)  
   
include $(CLEAR_VARS)  
LOCAL_MODULE :=  avutil-52-prebuilt  
LOCAL_SRC_FILES :=srcFFmpeg/prebuilt/libavutil-52.so  
include $(PREBUILT_SHARED_LIBRARY)  
   
include $(CLEAR_VARS)  
LOCAL_MODULE :=  avswresample-0-prebuilt  
LOCAL_SRC_FILES :=srcFFmpeg/prebuilt/libswresample-0.so  
include $(PREBUILT_SHARED_LIBRARY)  
   
include $(CLEAR_VARS)  
LOCAL_MODULE :=  swscale-2-prebuilt  
LOCAL_SRC_FILES :=srcFFmpeg/prebuilt/libswscale-2.so  
include $(PREBUILT_SHARED_LIBRARY)  
   
   
#libSDL2main=======================================
# Lei Xiaohua
include $(CLEAR_VARS)
LOCAL_MODULE := SDLVedio
SDL_PATH := ./srcSDL/
LOCAL_C_INCLUDES := $(LOCAL_PATH)/$(SDL_PATH)/include
# Add your application source files here...
LOCAL_SRC_FILES := $(SDL_PATH)/src/main/android/SDL_android_main.c \
	./main.c \ ./FrameDataPool.c \ ./FrameDataPool.h
LOCAL_SHARED_LIBRARIES := SDL2 avcodec-55-prebuilt avdevice-55-prebuilt avfilter-4-prebuilt avformat-55-prebuilt avutil-52-prebuilt swscale-2-prebuilt avswresample-0-prebuilt SDLVedio
LOCAL_LDLIBS := -lGLESv1_CM -lGLESv2 -llog
include $(BUILD_SHARED_LIBRARY)
   
#=================ffmpeg_coder=====================
#include $(CLEAR_VARS)  
#LOCAL_MODULE :=ffmpeg_codec  
#LOCAL_SRC_FILES :=ffmpegUtils.c  
#   
#LOCAL_LDLIBS := -lGLESv1_CM -lGLESv2 -llog
##LOCAL_ALLOW_UNDEFINED_SYMBOLS := true
#LOCAL_SHARED_LIBRARIES:= avcodec-55-prebuilt avdevice-55-prebuilt avfilter-4-prebuilt avformat-55-prebuilt avutil-52-prebuilt swscale-2-prebuilt avswresample-0-prebuilt SDLVedio
#include $(BUILD_SHARED_LIBRARY)  
