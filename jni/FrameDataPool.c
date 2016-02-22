#include "FrameDataPool.h"

/**
 * 音视频数据队列
 */

//视频帧数据相关函数
void initVedioFrameQueue(VedioFrameQueue * frameQueue) {
	memset(frameQueue, 0, sizeof(VedioFrameQueue));
	frameQueue->length = 0;
	frameQueue->queueHead = NULL;
	frameQueue->queueTail = NULL;
	frameQueue->vedioQueueLock = SDL_CreateMutex();
}

int pushVedioFrame(VedioFrameQueue *frameQueue,VedioQueueItem * pushItem) {
	int result = 0;
	SDL_LockMutex(frameQueue->vedioQueueLock);
//	if ((frameQueue->length) >= MAX_VEDIO_FRAME_QUEUE_LENGTH) {
//		result = -1;
//	}
//	else
	//队列操作加锁
	{
		if (frameQueue->length == 0) {
			frameQueue->queueHead = pushItem;
			frameQueue->queueTail = pushItem;
			frameQueue->length++;
		} else {
			frameQueue->queueTail->nextItem = pushItem;
			frameQueue->queueTail = pushItem;
			frameQueue->length++;
		}
		result = 0;
	}
	SDL_UnlockMutex(frameQueue->vedioQueueLock);
	return result;
}

int popVedioFrameData(VedioFrameQueue *frameQueue, VedioQueueItem * item) {
	int res = 0;
	SDL_LockMutex(frameQueue->vedioQueueLock);
	if (frameQueue->length == 0) {
		res= -1;
	} else {
		if (frameQueue->queueHead != NULL) {
			(*item) = *(frameQueue->queueHead);
			frameQueue->length--;
			if (frameQueue->length == 0) {
				frameQueue->queueHead = NULL;
				frameQueue->queueTail = NULL;
				res =-1;
			} else {
				frameQueue->queueHead = frameQueue->queueHead->nextItem;
			}
			item->nextItem = NULL;
		}
	}
	SDL_UnlockMutex(frameQueue->vedioQueueLock);
	return res;
}

double getVedioQueueTailPts(VedioFrameQueue *frameQueue){
	if(frameQueue->queueTail == NULL){
		return 0;
	}else{
		return frameQueue->queueTail->pts;
	}
}
/**
 * 清除全部位于队列中的元素
 */
void clearVideoQueueLength(VedioFrameQueue * frame_queue){
	SDL_LockMutex(frame_queue->vedioQueueLock);
	VedioQueueItem * tempItem = frame_queue->queueHead;
	while(tempItem != NULL){
		VedioQueueItem * nextItem = tempItem->nextItem;
		free(tempItem->vedioFrame);
		free(tempItem);
		tempItem = nextItem;
	}
	frame_queue->length = 0;
	LOGVEDIOTH("视频队列清除完成");
	SDL_UnlockMutex(frame_queue->vedioQueueLock);
	return;
}


//音频帧数据相关函数
void initAudioFrameQueue(AudioFrameQueue * frameQueue) {
	memset(frameQueue, 0, sizeof(AudioFrameQueue));
	frameQueue->length = 0;
	frameQueue->queueHead = NULL;
	frameQueue->queueTail = NULL;
	frameQueue->audioQueueLock = SDL_CreateMutex();
}

int pushAudioFrame(AudioFrameQueue * frameQueue , AudioQueueItem * item){
	int result = 0;
		SDL_LockMutex(frameQueue->audioQueueLock);
	//	if (frameQueue->length >= MAX_AUDIO_FRAME_QUEUE_LENGTH) {
	//		result = -1;
	//	} else
		{
			if (frameQueue->length == 0) {
				frameQueue->queueHead = item;
				frameQueue->queueTail = item;
				frameQueue->length++;
			} else {
				frameQueue->queueTail->nextItem = item;
				frameQueue->queueTail = item;
				frameQueue->length++;
			}
		}
		SDL_UnlockMutex(frameQueue->audioQueueLock);
		return result;
}

int popAudioFrameData(AudioFrameQueue *frameQueue, AudioQueueItem * item) {
	int res = 0;
	SDL_LockMutex(frameQueue->audioQueueLock);
	if (frameQueue->length == 0) {
		res = -1;
	} else {
		if (frameQueue->queueHead != NULL) {
			(*item) = *(frameQueue->queueHead);
			frameQueue->length--;
			if (frameQueue->length == 0) {
				frameQueue->queueHead = NULL;
				frameQueue->queueTail = NULL;
			} else {
				frameQueue->queueHead = frameQueue->queueHead->nextItem;
			}
			item->nextItem = NULL;
		} else {
			res =  -1;
		}
	}
	SDL_UnlockMutex(frameQueue->audioQueueLock);
}

/**
 * 清除全部位于队列中的元素
 */
void clearAudioQueueLength(AudioFrameQueue * frame_queue){
	SDL_LockMutex(frame_queue->audioQueueLock);
	AudioQueueItem * tempItem = frame_queue->queueHead;
	while(tempItem != NULL){
		AudioQueueItem * nextItem = tempItem->nextItem;
		free(tempItem->audioFrame);
		free(tempItem);
		tempItem = nextItem;
	}
	frame_queue->length = 0;
	LOGVEDIOTH("音频队列清除完成");
	SDL_UnlockMutex(frame_queue->audioQueueLock);
	return;
}
