#include <stdio.h>

#include "jni.h"
#include "srcSDL/include/SDL.h"
#include "android/log.h"
#include "srcFFmpeg/libavcodec/avcodec.h"
#include "srcFFmpeg/libswresample/swresample.h"
#include "srcFFmpeg/libavformat/avformat.h"
#include "srcFFmpeg/libswscale/swscale.h"
#include "FrameDataPool.h"
#include "time.h"

#define __STDC_CONSTANT_MACROS
//Android Log打印函数
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,"NDK_VEDIO_PLAY" ,__VA_ARGS__)
#define LOGVEDIOTH(...) __android_log_print(ANDROID_LOG_ERROR,"NDK_VEDIO_Thread" ,__VA_ARGS__)
//Refresh Event
#define SFM_REFRESH_EVENT  (SDL_USEREVENT + 1)
#define SFM_BREAK_EVENT  (SDL_USEREVENT + 2)
#define AUDIO_PLAY (SDL_USEREVENT + 3)
//音频帧大小
#define MAX_AUDIO_FRAME_SIZE 192000
//seek操作最大误差毫秒数
#define MAX_DELTA_TIME_MILLS 300
//视频播放结束误差值
#define DELTA_VEDIO_OVER_MILLS 500
//视频帧缓冲安全帧数量，高于此数量开始播放
#define CACHE_PLAY_FRAME_NUM 150
//音视频最大间隔时间，超出此时间将会暂停音频
#define AV_MAX_DELTA_TIME 2000

AVFormatContext *pFormatCtx;
//----------ffmpeg - vedio ------
int videoindex;
AVCodecContext *pCodecCtx;
AVCodec *pCodec;
AVFrame *pFrame, *pFrameYUV;
uint8_t *out_buffer;
int vedio_frame_length = 0;
AVPacket *packet;
int ret, got_picture;
int refresh_frame_time = 0;

//----------ffmepg - audio -------
int audio_index;
int audio_out_channel;
AVCodecContext * aCodecCtx;
AVCodec * aCodec;
AVFrame * aFrame;

//------------SDL----------------
int screen_w, screen_h;
SDL_Window *screen;
SDL_Renderer* sdlRenderer;
SDL_Texture* sdlTexture;
SDL_Rect play_win_rect;
SDL_Thread *video_tid;
SDL_Event event;
struct SwsContext *img_convert_ctx;
SDL_AudioSpec wanted_spec;

SDL_Thread * audio_tid;
struct SwrContext *au_convert_ctx;
uint8_t *audio_out_buffer;
int out_linesize;
AudioQueueItem * tempAudioItem;

static Uint8 *audio_chunk;
static Uint32 audio_len;
static Uint8 *audio_pos;

//=================全局变量
int thread_exit = 0;
int is_play_pause = 0;
int is_seeking = 0;
int isInitComplete = 0;
int isStartInit = 0;

//音频帧时间基
float tempATimeBase = 0;
//视频帧时间基
float tempTimeBase = 0;
long VEDIO_DURATION = 0;
//当前视频缓冲时间毫秒数
long cur_cache_time_mills = 0;
long TIME_COUNT_MILLS = 0;
long vedio_cur_time;
long audio_cur_time;

int skipVedioFrame = 0;
//是否视频解析已经完成
int isVedioDedocOver = 0;
char * playFilepath = NULL;

SDL_Thread * time_tid;

//JVM变量
static JavaVM * globle_vm;
static jclass vedio_instance = NULL;
static jmethodID onChangeMethodID = NULL;
static jobject vedioFrameInfo = NULL;

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	globle_vm = vm;
	return JNI_VERSION_1_4;
}

void NDK_C_OnPlayerStateChanged(int state);

/**
 * 視頻解碼線程
 */
int sfp_refresh_thread(void *opaque) {
	thread_exit = 0;
	while (thread_exit == 0) {
		//队列满的时候阻塞
		//当seek操作时暂停下载和解析进程
		if (is_seeking == 1) {
			SDL_Delay(1);
			continue;
		}

		while (audio_frame_queue->length >= MAX_AUDIO_FRAME_QUEUE_LENGTH
				&& vedio_frame_queue->length >= MAX_VEDIO_FRAME_QUEUE_LENGTH) {
			//block
			SDL_Delay(1);
		}

		if (av_read_frame(pFormatCtx, packet) >= 0) {
			//当解析到数据时说明并没有结束
			isVedioDedocOver = 0;
			if (packet->stream_index == videoindex) {
				ret = avcodec_decode_video2(pCodecCtx, pFrame, &got_picture,
						packet);
				if (ret < 0) {
					LOGE("Decode Error.\n");
					continue;
				}
				if (got_picture) {
					sws_scale(img_convert_ctx,
							(const uint8_t* const *) pFrame->data,
							pFrame->linesize, 0, pCodecCtx->height,
							pFrameYUV->data, pFrameYUV->linesize);
					//添加视频到缓冲区
					VedioQueueItem *pushItem = (VedioQueueItem *) av_mallocz(
							sizeof(VedioQueueItem));
					uint8_t * tempData = (uint8_t *) malloc(
							sizeof(uint8_t) * vedio_frame_length);
					memcpy(tempData, pFrameYUV->data[0], vedio_frame_length);
					pushItem->vedioFrame = tempData;
					pushItem->length = vedio_frame_length;
					pushItem->video_height = pCodecCtx->height;
					pushItem->video_width = pCodecCtx->width;
					pushItem->pts = packet->pts;
					pushVedioFrame(vedio_frame_queue, pushItem);
				} else {
					LOGE("DECODE解析到错误帧,结尾帧数量：%d", skipVedioFrame);
					skipVedioFrame++;
				}
			} else if (packet->stream_index == audio_index) {
				ret = avcodec_decode_audio4(aCodecCtx, aFrame, &got_picture,
						packet);
				if (ret < 0) {
					LOGE("音频解析失败");
					continue;
				}
				if (got_picture) {
					swr_convert(au_convert_ctx, &audio_out_buffer,
					MAX_AUDIO_FRAME_SIZE, (const uint8_t **) aFrame->data,
							aFrame->nb_samples);
					AudioQueueItem *pushItem = (AudioQueueItem *) av_mallocz(
							sizeof(AudioQueueItem));

					int audio_out_ch = av_get_channel_layout_nb_channels(
					AV_CH_LAYOUT_STEREO);
					int audio_frame_length = av_samples_get_buffer_size(NULL,
							audio_out_ch, aFrame->nb_samples, AV_SAMPLE_FMT_S16,
							1);
					uint8_t * tempFrameData = (uint8_t *) malloc(
							sizeof(uint8_t) * MAX_AUDIO_FRAME_SIZE * 2);
					memcpy(tempFrameData, audio_out_buffer,
					MAX_AUDIO_FRAME_SIZE * 2);

//					LOGVEDIOTH("解码音频帧长度：%d ， 管道：%d , 采样率：%d",
//							audio_frame_length, audio_out_channel,
//							aFrame->nb_samples);
					pushItem->audioFrame = tempFrameData;
					pushItem->pts = packet->pts;
					pushItem->frameLength = audio_frame_length;
					pushItem->audioFrameSamples = aFrame->nb_samples;
					pushItem->nextItem = NULL;
					pushAudioFrame(audio_frame_queue, pushItem);
				}
			}
			//视频缓冲时间更新
			NDK_C_OnPlayerStateChanged(6);
			av_free_packet(packet);
		}
		//当读取失败，即视频到达结尾处时
		else {
			isVedioDedocOver = 1;
		}
	}
	return 0;
}

void fill_audio(void *udata, Uint8 *stream, int len) {
	/*  Only  play  if  we  have  data  left  */
	SDL_memset(stream, 0, len); // make sure this is silence.
	if (audio_len == 0)
		return;
	/*  Mix  as  much  data  as  possible  */
	len = (len > audio_len ? audio_len : len);
	SDL_MixAudio(stream, audio_pos, len, SDL_MIX_MAXVOLUME);
	audio_pos += len;
	audio_len -= len;
}

/**
 * 音頻播放線程
 */
void th_audio_play(void *opaque) {
	while (thread_exit == 0) {
		//当播放暂停时，阻塞线程
		if (is_play_pause != 0) {
			SDL_Delay(1);
			continue;
		}

		//当视频缓冲数量低于此安全线时，同样暂停音频播放
		if(!isVedioDedocOver){
			if (vedio_frame_queue->length <= CACHE_PLAY_FRAME_NUM) {
				//LOGE("当前对列数据量：%d", vedio_frame_queue->length);
				SDL_Delay(5);
				continue;
			}

			/**
			* 如果音频比视频速度过快，强制暂停音频进程
			*/
			if ((audio_cur_time - vedio_cur_time) > AV_MAX_DELTA_TIME) {
				SDL_Delay(AV_MAX_DELTA_TIME);
				continue;
			}
		}

		if (audio_frame_queue->length == 0) {
			SDL_Delay(1);
			continue;
		}

		tempAudioItem = (AudioQueueItem *) av_mallocz(sizeof(AudioQueueItem));
		popAudioFrameData(audio_frame_queue, tempAudioItem);

		if (wanted_spec.samples != tempAudioItem->audioFrameSamples) {
			SDL_CloseAudio();
			wanted_spec.samples = tempAudioItem->audioFrameSamples;
			SDL_OpenAudio(&wanted_spec, NULL);
			//LOGE("修正播放采样率为：%d", aFrame->nb_samples);
		}

		audio_chunk = tempAudioItem->audioFrame;
		audio_len = tempAudioItem->frameLength;
		audio_pos = audio_chunk;
		//记录当前音频播放时间
		audio_cur_time = (tempAudioItem->pts * 1000) / tempATimeBase;
		TIME_COUNT_MILLS = audio_cur_time;
		SDL_PauseAudio(0);
		while (audio_len > 0) {
			SDL_Delay(1);
		}
		//释放音频帧资源
		free(tempAudioItem->audioFrame);
		free(tempAudioItem);
	}
}

//视频播放计时线程
int th_time_count(void *opaque) {
	while (thread_exit == 0) {
		TIME_COUNT_MILLS++;
		SDL_Delay(1);
	}
	return 0;
}

int main(int argc, char* argv[]) {
	//初始化视频缓冲区
	vedio_frame_queue = (VedioFrameQueue *) malloc(sizeof(VedioFrameQueue));
	audio_frame_queue = (AudioFrameQueue *) malloc(sizeof(AudioFrameQueue));
	initVedioFrameQueue(vedio_frame_queue);
	initAudioFrameQueue(audio_frame_queue);

	//阻塞等待播放
	while (isStartInit == 0) {
		SDL_Delay(1);
		continue;
	}

	LOGE("初始化完成，开始播放");

	av_register_all();
	avformat_network_init();

	pFormatCtx = avformat_alloc_context();
	if (pFormatCtx == NULL) {
		LOGE("Couldn't open input stream.\n");
		return -1;
	}

	if (avformat_open_input(&pFormatCtx, playFilepath, NULL, NULL) != 0) {
		thread_exit = 0;
		//Break
		SDL_Event event;
		event.type = SFM_BREAK_EVENT;
		SDL_PushEvent(&event);
		return 0;
	}

	if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
		LOGE("Couldn't find stream information.\n");
		return -1;
	}
	videoindex = -1;
	int i = 0;
	for (i = 0; i < pFormatCtx->nb_streams; i++) {
		if (pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
			videoindex = i;
			break;
		}
	}
	audio_index = -1;
	for (i = 0; i < pFormatCtx->nb_streams; i++) {
		if (pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
			audio_index = i;
			break;
		}
	}

	if (videoindex == -1) {
		LOGE("Didn't find a video stream.\n");
		return -1;
	}

	pCodecCtx = pFormatCtx->streams[videoindex]->codec;
	pCodec = avcodec_find_decoder(pCodecCtx->codec_id);
	if (pCodec == NULL) {
		LOGE("Codec not found.\n");
		return -1;
	}
	if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
		LOGE("Could not open codec.\n");
		return -1;
	}
	pFrame = av_frame_alloc();
	pFrameYUV = av_frame_alloc();
	out_buffer = (uint8_t *) av_malloc(
			avpicture_get_size(PIX_FMT_YUV420P, pCodecCtx->width,
					pCodecCtx->height));
	avpicture_fill((AVPicture *) pFrameYUV, out_buffer, PIX_FMT_YUV420P,
			pCodecCtx->width, pCodecCtx->height);

	vedio_frame_length = avpicture_get_size(PIX_FMT_YUV420P, pCodecCtx->width,
			pCodecCtx->height);
	img_convert_ctx = sws_getContext(pCodecCtx->width, pCodecCtx->height,
			pCodecCtx->pix_fmt, pCodecCtx->width, pCodecCtx->height,
			PIX_FMT_YUV420P,
			SWS_BICUBIC, NULL, NULL, NULL);

	float tempFps = (float) pFormatCtx->streams[videoindex]->avg_frame_rate.num
			/ pFormatCtx->streams[videoindex]->avg_frame_rate.den;

	VEDIO_DURATION = (pFormatCtx->duration / 1000);
	refresh_frame_time = 1000 / tempFps;
	LOGVEDIOTH("视频帧率：%d", refresh_frame_time);

	////初始化audio解析部分
	if (audio_index == -1) {
		LOGE("Didn't find a audio stream.\n");
		return -1;
	}
	aCodecCtx = pFormatCtx->streams[audio_index]->codec;
	if (aCodecCtx == NULL) {
		LOGE("音频初始化失败");
		return -1;
	}
	aCodec = avcodec_find_decoder(aCodecCtx->codec_id);
	if (avcodec_open2(aCodecCtx, aCodec, NULL) < 0) {
		LOGE("音频解码器打开失败");
		return -1;
	}
	aFrame = av_frame_alloc();

	//输出内存大小
	audio_out_buffer = (uint8_t *) malloc(
			sizeof(uint8_t) * MAX_AUDIO_FRAME_SIZE * 2);
	au_convert_ctx = swr_alloc();
	int in_audio_channel_layout = av_get_default_channel_layout(
			aCodecCtx->channels);
	au_convert_ctx = swr_alloc_set_opts(au_convert_ctx, AV_CH_LAYOUT_STEREO,
			AV_SAMPLE_FMT_S16, 44100, in_audio_channel_layout,
			aCodecCtx->sample_fmt, aCodecCtx->sample_rate, 0, NULL);
	swr_init(au_convert_ctx);

	tempATimeBase = (float) pFormatCtx->streams[audio_index]->time_base.den
			/ pFormatCtx->streams[audio_index]->time_base.num;
	//记录此帧视频播放时间
	tempTimeBase = (float) pFormatCtx->streams[videoindex]->time_base.den
			/ pFormatCtx->streams[videoindex]->time_base.num;

	//初始化SDL部分
	if (SDL_Init(SDL_INIT_VIDEO | SDL_INIT_AUDIO | SDL_INIT_TIMER)) {
		LOGE("Could not initialize SDL - %s\n", SDL_GetError());
		return -1;
	}
	//SDL 2.0 Support for multiple windows
	screen_w = pCodecCtx->width;
	screen_h = pCodecCtx->height;
	screen = SDL_CreateWindow("Simplest ffmpeg player's Window",
	SDL_WINDOWPOS_UNDEFINED, SDL_WINDOWPOS_UNDEFINED, screen_w, screen_h,
			SDL_WINDOW_OPENGL);

	if (!screen) {
		LOGE("SDL: could not create window - exiting:%s\n", SDL_GetError());
		return -1;
	}
	sdlRenderer = SDL_CreateRenderer(screen, -1, 0);
	//IYUV: Y + U + V  (3 planes)
	//YV12: Y + V + U  (3 planes)
	sdlTexture = SDL_CreateTexture(sdlRenderer, SDL_PIXELFORMAT_IYUV,
			SDL_TEXTUREACCESS_STREAMING, pCodecCtx->width, pCodecCtx->height);

	int w, h;
	SDL_GetWindowSize(screen, &w, &h);
	play_win_rect.x = 0;
	play_win_rect.y = 0;
	play_win_rect.w = w;
	play_win_rect.h = h;

	//用于获取视频或音频帧数据包，
	packet = (AVPacket *) av_malloc(sizeof(AVPacket));

	//初始化SDL音频部分
	wanted_spec.freq = 44100;
	wanted_spec.format = AUDIO_S16SYS;
	wanted_spec.channels = av_get_channel_layout_nb_channels(
	AV_CH_LAYOUT_STEREO);
	;
	wanted_spec.silence = 0;
	wanted_spec.samples = aCodecCtx->frame_size;
	wanted_spec.callback = fill_audio;
	wanted_spec.userdata = aCodecCtx;

	if (SDL_OpenAudio(&wanted_spec, NULL) < 0) {
		LOGE("SDL音频设备开启失败:%s", SDL_GetError());
		return -1;
	}

	LOGE("比特率 %3d\n", pFormatCtx->bit_rate);
	LOGE("解码器名称 %s\n", aCodecCtx->codec->long_name);
	//LOGE("time_base:%d \n", aCodecCtx->time_base.num);
	//LOGE("声道数  %d \n", aCodecCtx->channels.num);
	LOGE("sample per second  %d \n", aCodecCtx->sample_rate);

	NDK_C_OnPlayerStateChanged(1);
	video_tid = SDL_CreateThread(sfp_refresh_thread, NULL, NULL);
	audio_tid = SDL_CreateThread(th_audio_play, NULL, NULL);
	time_tid = SDL_CreateThread(th_time_count, NULL, NULL);

	thread_exit = 0;
	//用于计算每一帧绘制用时，以平衡视频帧暂停时间
	clock_t cricleStartTime;
	clock_t cricleEndTime;
	while (thread_exit == 0) {
		//Wait
		if (is_play_pause != 0) {
			SDL_Delay(1);
			continue;
		}

		if(!isVedioDedocOver){
			//当视频缓冲数量低于此安全线时，暂停播放
			if (vedio_frame_queue->length <= CACHE_PLAY_FRAME_NUM) {
				//通知Java层缓冲位置更新
				SDL_Delay(100);
				LOGE("视频正在缓冲");
				continue;
			}
		}

		//记录开始时间
		cricleStartTime = clock();

		VedioQueueItem * tempFrame = (VedioQueueItem *) av_mallocz(
				sizeof(VedioQueueItem));
		popVedioFrameData(vedio_frame_queue, tempFrame);
		if (tempFrame->vedioFrame == NULL) {
			free(tempFrame);
			continue;
		}
		//获取当前视频播放时间
		vedio_cur_time = (tempFrame->pts * 1000) / tempTimeBase;
		int resUp = SDL_UpdateTexture(sdlTexture, NULL, tempFrame->vedioFrame,
				tempFrame->video_height);
		int resClear = SDL_RenderClear(sdlRenderer);
		int resCopy = SDL_RenderCopy(sdlRenderer, sdlTexture, NULL,
				&play_win_rect);
		SDL_RenderPresent(sdlRenderer);
		//通知Java层
		NDK_C_OnPlayerStateChanged(2);
		free(tempFrame->vedioFrame);
		free(tempFrame);

		//记录结束时间
		cricleEndTime = clock();
		int circleNeedTime = (cricleEndTime - cricleStartTime) / 10000;
		//LOGE("视频绘制用时：%dms", circleNeedTime);
		int sleepTime = refresh_frame_time;
		//当视频播放慢于音频播放时，视频播放刷新时间减少
		if (vedio_cur_time < TIME_COUNT_MILLS) {
			sleepTime -= 30;
		}
		if (circleNeedTime > refresh_frame_time) {
			sleepTime = 0;
		} else {
			sleepTime -= circleNeedTime;
		}
		if (sleepTime < 0) {
			sleepTime = 0;
		}
		//LOGE("视频帧暂停时间：%d", sleepTime);
		//LOGE("当前标准时间：%d,，当前视频时间：%d", TIME_COUNT_MILLS, vedio_cur_time);
		SDL_Delay(sleepTime);

		//判断视频播放时候结束
		//当视频剩余播放时间小于500ms时，并且视频队列为空时，结束播放，向Java层发送结束信息
		if (isVedioDedocOver && vedio_frame_queue->length == 0) {
			LOGE("视频播放结束");
			NDK_C_OnPlayerStateChanged(3);
		}
		//LOGE("是否已经解码结束：%d , 视频全场：%ld , 视频剩余时间：%ld , 结束时间：%ld",isVedioDedocOver, VEDIO_DURATION,VEDIO_DURATION - vedio_cur_time, VEDIO_DURATION);
	}
	return 0;
}

//===========================工具函数==================================
char* Jstring2CStr(JNIEnv* env, jstring jstr) {
	char* rtn = NULL;
	jclass clsstring = (*env)->FindClass(env, "java/lang/String");
	jstring strencode = (*env)->NewStringUTF(env, "GB2312");
	jmethodID mid = (*env)->GetMethodID(env, clsstring, "getBytes",
			"(Ljava/lang/String;)[B");
	jbyteArray barr = (jbyteArray)(*env)->CallObjectMethod(env, jstr, mid,
			strencode);
	jsize alen = (*env)->GetArrayLength(env, barr);
	jbyte* ba = (*env)->GetByteArrayElements(env, barr, JNI_FALSE);
	if (alen > 0) {
		rtn = (char*) malloc(alen + 1);
		memcpy(rtn, ba, alen);
		rtn[alen] = 0;
	}
	(*env)->ReleaseByteArrayElements(env, barr, ba, 0);  //释放内存
	return rtn;
}
/**
 * 状态改变之后通知Java层
 */
void NDK_C_OnPlayerStateChanged(int state) {
	JNIEnv *env = NULL;
	(*globle_vm)->AttachCurrentThread(globle_vm, &env, NULL);
	(*env)->CallStaticVoidMethod(env, vedio_instance, onChangeMethodID, state);
	//(*globle_vm)->DetachCurrentThread(globle_vm);    //释放当前线程的JNIEnv*
}
//=============================控制函数==============================

jint Java_com_example_customview_VedioView_initVedioPlayer(JNIEnv* env,
		jclass cls, jstring filepath) {
	playFilepath = Jstring2CStr(env, filepath);
	if (NULL == vedio_instance) {
		jclass temp_vedio_instance = (*env)->FindClass(env,
				"com/example/customview/VedioView");
		vedio_instance = (jclass)(
				(*env)->NewGlobalRef(env, temp_vedio_instance));
		LOGE("1");
		onChangeMethodID = (*env)->GetStaticMethodID(env, vedio_instance,
				"NDK_C_OnVedioPlayerStateChanged", "(I)V");
	}
	//初始化完成
	isStartInit = 1;
	return 0;
}

void Java_com_example_customview_VedioView_nativePauseVedio(JNIEnv * env,
		jobject obj) {
	is_play_pause = 1;
	NDK_C_OnPlayerStateChanged(4);
}

void Java_com_example_customview_VedioView_nativeResumeVedio(JNIEnv * env,
		jobject obj) {
	is_play_pause = 0;
	NDK_C_OnPlayerStateChanged(5);
}

/**
 * 停止视频播放
 */
void Java_com_example_customview_VedioView_nativeStopPlay(JNIEnv * env,
		jobject obj) {
	thread_exit = 1;
	//终止三个线程
	sws_freeContext(img_convert_ctx);

	//--------------
	avcodec_close(pCodecCtx);
	avcodec_close(aCodecCtx);
	av_free(pCodec);
	av_free(aCodec);
	av_free(aFrame);
	av_free(pFrameYUV);
	av_free(pFrame);
	free(img_convert_ctx);
	free(out_buffer);
	free(au_convert_ctx);
	free(audio_out_buffer);
	free(tempAudioItem);
	free(audio_chunk);
	free(audio_pos);
	free(playFilepath);
	av_free_packet(packet);
	clearVideoQueueLength(vedio_frame_queue);
	clearAudioQueueLength(audio_frame_queue);
	avformat_close_input(&pFormatCtx);
	(*globle_vm)->DetachCurrentThread(globle_vm);    //释放当前线程的JNIEnv*
	SDL_Quit();
	//通知Java层代码
	NDK_C_OnPlayerStateChanged(3);
}
/**
 * 修改播放窗口大小的函数
 * 参数 左上点坐标 、 右上点坐标
 */
void Java_com_example_customview_VedioView_changePlayWindow(JNIEnv *env,
		jobject obj, jint window_TLX, jint window_TLY, jint window_RBX,
		jint window_RBY) {
	play_win_rect.x = window_TLX;
	play_win_rect.y = window_TLY;
	play_win_rect.h = (window_RBY - window_TLY);
	play_win_rect.w = (window_RBX - window_TLX);
}

/**
 * 视频跳转控制函数
 */
void Java_com_example_customview_VedioView_seekVedioFrame(JNIEnv * env,
		jobject obj, jlong seek_time) {
	//seek过程中暂停播放和下载
	is_play_pause = 1;
	NDK_C_OnPlayerStateChanged(5);
	is_seeking = 1;
	seek_time *= 1000;
	//判断两者是否都位于当前缓冲区中
	int isFindAudio = 0, isFindVedio = 0;
	while (vedio_frame_queue->length != 0) {
		VedioQueueItem * tempFrame = (VedioQueueItem *) av_mallocz(
				sizeof(VedioQueueItem));
		popVedioFrameData(vedio_frame_queue, tempFrame);

		long tempVedioTime = (tempFrame->pts * 1000) / tempTimeBase;
		if ((seek_time - tempVedioTime) < MAX_DELTA_TIME_MILLS) {
			isFindVedio = 1;
			break;
		}
		free(tempFrame->vedioFrame);
		free(tempFrame);
	}
	LOGE("跳转：视频，是否找到队列帧%d", isFindVedio);
	while (vedio_frame_queue->length != 0) {
		AudioQueueItem * tempFrame = (AudioQueueItem *) av_mallocz(
				sizeof(AudioQueueItem));
		popAudioFrameData(vedio_frame_queue, tempFrame);
		long tempVedioTime = (tempFrame->pts * 1000) / tempATimeBase;
		if ((seek_time - tempVedioTime) < MAX_DELTA_TIME_MILLS) {
			isFindAudio = 1;
			break;
		}
		free(tempFrame->audioFrame);
		free(tempFrame);
	}
	LOGE("跳转：音频，是否找到队列帧%d", isFindAudio);
	//当音频帧和视频帧均找到时直接开始播放,否则清除全部队列
	if (isFindAudio != 1 && isFindVedio != 1) {
		clearAudioQueueLength(audio_frame_queue);
		clearVideoQueueLength(vedio_frame_queue);
		int res = av_seek_frame(pFormatCtx, -1, seek_time,
		AVSEEK_FLAG_ANY);
	}
	//继续播放和下载
	is_seeking = 0;
	is_play_pause = 0;
	NDK_C_OnPlayerStateChanged(5);
}

//===========================用于获取信息的函数==========================
jobject Java_com_example_customview_VedioView_getOpenedFileInfo(JNIEnv * env,
		jobject obj) {
	if (pFormatCtx == NULL) {
		LOGE("未打开文件");
		return NULL;
	}

	jclass clazz;
	jclass tempclazz = (*env)->FindClass(env, "org/libsdl/app/VedioBaseInfo");
	clazz = (jclass)(*env)->NewGlobalRef(env, tempclazz);

	jobject resObj = (*env)->AllocObject(env, clazz);

	jfieldID timeID = (*env)->GetFieldID(env, clazz, "vedioTimeLength", "I");

	jfieldID heightID = (*env)->GetFieldID(env, clazz, "vedioHeight", "I");

	jfieldID weightID = (*env)->GetFieldID(env, clazz, "vedioWidth", "I");

	jfieldID windowHeightID = (*env)->GetFieldID(env, clazz, "windowHeight",
			"I");
	jfieldID windowWidthID = (*env)->GetFieldID(env, clazz, "windowWidth", "I");
	jfieldID vedioCodexTypeID = (*env)->GetFieldID(env, clazz, "vedioCodecType",
			"I");
	jfieldID vedioBitRateID = (*env)->GetFieldID(env, clazz, "bitRate", "I");
	jfieldID fpsID = (*env)->GetFieldID(env, clazz, "vedio_fps", "F");
	jfieldID timeBaseID = (*env)->GetFieldID(env, clazz, "vedio_time_base",
			"F");
	jfieldID vedioCodecName = (*env)->GetFieldID(env, clazz, "vedioCodecName",
			"Ljava/lang/String;");

	jfieldID audioBitRateID = (*env)->GetFieldID(env, clazz, "audioBitRate",
			"I");
	jfieldID audioTiemBaseID = (*env)->GetFieldID(env, clazz, "audioTimebase",
			"F");
	jfieldID audioCodecNameID = (*env)->GetFieldID(env, clazz, "audioCodecName",
			"Ljava/lang/String;");
	jfieldID audioChannelsID = (*env)->GetFieldID(env, clazz, "audioChannels",
			"I");
	jfieldID audioSampleRateID = (*env)->GetFieldID(env, clazz,
			"audioSampleRate", "I");

	(*env)->SetIntField(env, resObj, weightID, pCodecCtx->width);
	(*env)->SetIntField(env, resObj, heightID, pCodecCtx->height);
	(*env)->SetIntField(env, resObj, timeID, pFormatCtx->duration / 1000);
	(*env)->SetIntField(env, resObj, vedioCodexTypeID, pCodecCtx->codec_type);
	(*env)->SetIntField(env, resObj, vedioBitRateID, pCodecCtx->bit_rate);
	(*env)->SetObjectField(env, resObj, vedioCodecName,
			(*env)->NewStringUTF(env, pCodecCtx->codec->long_name));
	float tempFps = (float) pFormatCtx->streams[videoindex]->avg_frame_rate.den
			/ pFormatCtx->streams[videoindex]->avg_frame_rate.num;
	(*env)->SetFloatField(env, resObj, fpsID, tempFps);
	float tempTimeBase = (float) pFormatCtx->streams[videoindex]->time_base.den
			/ pFormatCtx->streams[videoindex]->time_base.num;
	(*env)->SetFloatField(env, resObj, timeBaseID, tempTimeBase);

	if (aCodecCtx != NULL) {
		(*env)->SetIntField(env, resObj, audioBitRateID, aCodecCtx->bit_rate);
		float tempAudioTimeBase = (float) aCodecCtx->time_base.den
				/ aCodecCtx->time_base.num;
		(*env)->SetFloatField(env, resObj, audioTiemBaseID, tempAudioTimeBase);
		(*env)->SetObjectField(env, resObj, audioCodecNameID,
				(*env)->NewStringUTF(env, aCodecCtx->codec->long_name));
		(*env)->SetIntField(env, resObj, audioChannelsID, aCodecCtx->channels);
		(*env)->SetIntField(env, resObj, audioSampleRateID,
				aCodecCtx->sample_rate);
	}
	return resObj;
}
//获取当前播放时间
jlong Java_com_example_customview_VedioView_getCurVedioPlayTime(JNIEnv * env,
		jobject obj) {
	return vedio_cur_time;
}

jdouble Java_com_example_customview_VedioView_getCacheVedioTime(JNIEnv * env,
		jobject obj) {
	double tempPts = getVedioQueueTailPts(vedio_frame_queue);
	return tempPts * 1000 / tempTimeBase;
}

jdouble Java_com_example_customview_VedioView_getCacheVedioFrameRate(
		JNIEnv * env, jobject obj) {
	return (double) (vedio_frame_queue->length) / CACHE_PLAY_FRAME_NUM;
}

//jobject Java_com_example_customview_VedioView_getReadedFrameInfo(JNIEnv * env,
//		jobject obj) {
//	if (packet == NULL) {
//		LOGE("未读取到视频帧数据");
//		return vedioFrameInfo;
//	}
//
//	if (lastFrameKind != 0 && vedioFrameInfo != NULL) {
//		return vedioFrameInfo;
//	}
//	jclass clazz = (*env)->FindClass(env, "org/libsdl/app/VedioFrameInfo");
//	if (vedioFrameInfo == NULL) {
//		jobject tempFrameInfo = (*env)->AllocObject(env, clazz);
//		vedioFrameInfo = (*env)->NewGlobalRef(env, tempFrameInfo);
//	}
//
//	jfieldID durationID = (*env)->GetFieldID(env, clazz, "vedioDuration", "J");
//	jfieldID playTimeID = (*env)->GetFieldID(env, clazz, "playTime", "J");
//	jfieldID isKeyFrame = (*env)->GetFieldID(env, clazz, "isKeyFrame", "I");
//	jfieldID frameKindID = (*env)->GetFieldID(env, clazz, "frameKind", "I");
//
//	float tempTimeBase = (float) pFormatCtx->streams[videoindex]->time_base.den
//			/ pFormatCtx->streams[videoindex]->time_base.num;
//	(*env)->SetLongField(env, vedioFrameInfo, durationID,
//			packet->duration * 1000 / tempTimeBase);
//	(*env)->SetLongField(env, vedioFrameInfo, playTimeID,
//			packet->pts * 1000 / tempTimeBase);
//	LOGE("PTS = %lld , duration = %lld , timebaseNUM = %d , timeBaseDEN = %lld",
//			packet->pts, packet->duration,
//			pFormatCtx->streams[videoindex]->time_base.num,
//			pFormatCtx->streams[videoindex]->time_base.den);
//	(*env)->SetIntField(env, vedioFrameInfo, isKeyFrame, pFrame->key_frame);
//	(*env)->SetIntField(env, vedioFrameInfo, frameKindID, pFrame->pict_type);
//	return vedioFrameInfo;
//}

