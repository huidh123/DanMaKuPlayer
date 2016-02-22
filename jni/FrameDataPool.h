#ifndef _FRAMEDATAPOLL_H
#define _FRAMEDATAPOLL_H

#include <stdio.h>
#include "sdl_thread.h"
#include "android/log.h"

//音视频队列最大缓冲帧数（参考值）可能会超出
#define MAX_VEDIO_FRAME_QUEUE_LENGTH 200
#define MAX_AUDIO_FRAME_QUEUE_LENGTH 400

#define LOGVEDIOTH(...) __android_log_print(ANDROID_LOG_ERROR,"NDK_VEDIO_Thread" ,__VA_ARGS__)

//视频帧数据队列Item结构体
typedef struct VedioFrameItemStruct {
	uint8_t * vedioFrame;
	double pts;
	int video_height;
	int video_width;
	int length;
	struct VedioFrameItemStruct * nextItem;
} VedioQueueItem;

//视频帧数据队列
typedef struct {
	VedioQueueItem * queueHead;
	VedioQueueItem * queueTail;
	int length;
	SDL_mutex * vedioQueueLock;
} VedioFrameQueue;

//音频帧数据队列Item结构体
typedef struct AudioFrameItemStruct {
	uint8_t * audioFrame;
	double pts;
	int frameLength;
	int audioFrameSamples;
	struct AudioFrameItemStruct * nextItem;
} AudioQueueItem;

typedef struct {
	AudioQueueItem * queueHead;
	AudioQueueItem * queueTail;
	int length;
	SDL_mutex * audioQueueLock;
} AudioFrameQueue;

//两个实例
VedioFrameQueue * vedio_frame_queue;
AudioFrameQueue * audio_frame_queue;

void initVedioFrameQueue(VedioFrameQueue * frameQueue);
int pushVedioFrame(VedioFrameQueue * frameQueue, VedioQueueItem * pushItem);
double getVedioQueueTailPts(VedioFrameQueue *frameQueue);
int popVedioFrameData(VedioFrameQueue * frameQueue,VedioQueueItem * item);
void initAudioFrameQueue(AudioFrameQueue * frameQueue);
int pushAudioFrame(AudioFrameQueue * frameQueue,AudioQueueItem *item);
int popAudioFrameData(AudioFrameQueue * frameQueue,AudioQueueItem * item);
int getVideoQueueQueueLength(VedioFrameQueue * frame_queue);

#endif
