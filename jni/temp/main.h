#ifndef _MAIN_H_
#define _MAIN_H_

#include <stdlib.h>
#include <stdio.h>
#include "sys/types.h"
#include "sys/stat.h"
#include "fcntl.h"
#include "time.h"
#include "unistd.h"
#include "jni.h"
#include "android/log.h"
#include "srcSDL/include/SDL.h"

#define REFRESH_EVENT (SDL_USEREVENT + 1)
#define QUIT_EVENT (SDL_USEREVENT +2)
#define PLAY_AUDIO (SDL_USEREVENT + 3)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,__VA_ARGS__,"NDK VEDIO")
#define LOGA(...) __android_log_print(ANDROID_LOG_ERROR,__VA_ARGS__,"NDK AUDIO")

Uint8 *audio_chunk;
Uint32 audio_len;
Uint8 *audio_pos;


jint Java_org_libsdl_app_VedioNativeHolder_setVedioFrameData(JNIEnv *env,
		jobject obj, jcharArray frameData);

extern void sdl_post_audio();

#endif
