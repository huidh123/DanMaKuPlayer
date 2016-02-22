/* Include the SDL main definition header */
#include "main.h"


//视频部分参数和全局变量
static int pixel_w = 640, pixel_h = 352;
SDL_Window *window;
SDL_Renderer *renderer;
SDL_Texture * vedio_texture;
//音频部分播放参数和全局变量
static int mAudioBitRate;
static int mAudioChannels;

SDL_AudioSpec * wanted_spec;
//Java层传送来的视频帧数据的缓冲区
jbyte * buffer;

//播放窗口尺寸
static SDL_Rect * play_win_rect;
//音频播放线程
SDL_Thread * audio_thread;
int au_thread_quit = 0;

//绘制线程锁
static int isLocked = 0;
static int isAudioInitSuccess = 0;

jbyte * audio_buffer;

char output_buffer[1024];

void fill_audio(void *udata, Uint8 *stream, int len) {
	//SDL 2.0
	SDL_memset(stream, 0, len);
	if (audio_len == 0) /*  Only  play  if  we  have  data  left  */
		return;
	len = (len > audio_len ? audio_len : len); /*  Mix  as  much  data  as  possible  */

	SDL_MixAudio(stream, audio_pos, len, SDL_MIX_MAXVOLUME);
	audio_pos += len;
	audio_len -= len;
}

int audio_thread_run(void * data) {
	SDL_Event event;
	while (au_thread_quit == 0) {
		SDL_WaitEvent(&event);
		if (event.type == PLAY_AUDIO) {
			SDL_PauseAudio(0);
			LOGE("播放音乐");
			while (audio_len > 0) {
				SDL_Delay(1);
			}
		}
	}
	return 0;
}

int main(int argc, char *argv[]) {

	extern Uint8 *audio_chunk;
	extern Uint32 audio_len;
	extern Uint8 *audio_pos;

	LOGE("SDL进入初始化函数");
	SDL_Init(SDL_INIT_VIDEO | SDL_INIT_AUDIO);
	window = SDL_CreateWindow("Simplest Video Play SDL2",
	SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED, pixel_w, pixel_h,
			SDL_WINDOW_OPENGL | SDL_WINDOW_FULLSCREEN);
	if (window == NULL) {
		LOGE("窗口创建失败");
		sprintf(output_buffer, "%s", SDL_GetError());
		LOGE(output_buffer);
		return -1;
	}
	int w, h;
	SDL_GetWindowSize(window, &w, &h);
	sprintf(output_buffer, "窗口初始化成功：宽度：%d, 高度：%d", w, h);
	LOGE(output_buffer);

	renderer = SDL_CreateRenderer(window, -1, 0);
	if (renderer == NULL) {
		LOGE("渲染器创建失败");
		return -1;
	}
	LOGE("渲染器创建成功");
	vedio_texture = SDL_CreateTexture(renderer, SDL_PIXELFORMAT_IYUV,
			SDL_TEXTUREACCESS_STREAMING, pixel_w, pixel_h);

	if (vedio_texture == NULL) {
		LOGE("SDL texture初始化失败");
		sprintf(output_buffer, "Texture高度：%d , 宽度：%d", pixel_h, pixel_w);
		LOGE(output_buffer);
		sprintf(output_buffer, "错误原因：%s", SDL_GetError());
		LOGE(output_buffer);
		return -1;
	}
	LOGE("texture初始化成功");

	//开始初始化音频部分
	SDL_AudioSpec wanted_spec;
	wanted_spec.freq = mAudioBitRate;
	wanted_spec.format = AUDIO_S16SYS;
	wanted_spec.channels = mAudioChannels;
	wanted_spec.silence = 0;
	wanted_spec.samples = 1024;
	wanted_spec.callback = fill_audio;
	if (SDL_OpenAudio(&wanted_spec, NULL) < 0) {
		LOGE("SDL音频初始化失败");
		sprintf(output_buffer, "失败原因：%s", SDL_GetError());
		LOGE(output_buffer);
	} else {
		isAudioInitSuccess = 1;
	}

	audio_thread = SDL_CreateThread(audio_thread_run, "audio_decode_thread",
	NULL);

	LOGE("SDL初始化成功");
	play_win_rect = (SDL_Rect *) malloc(sizeof(SDL_Rect));
	play_win_rect->x = 0;
	play_win_rect->y = 0;
	play_win_rect->w = w;
	play_win_rect->h = h;

	int isThreadExit = 0;
	SDL_Event event;
	while (!isThreadExit) {
		SDL_WaitEvent(&event);
		if (event.type == QUIT_EVENT) {
			LOGE("SDL通知接收");
			isThreadExit = 0;
			break;
		} else if (event.type == REFRESH_EVENT) {
			LOGE("执行绘制任务");
			if (buffer == NULL) {
				LOGE("数据错误，帧数据为空");
				continue;
			}
			sprintf(output_buffer, "执行刷新任务，更新材质，%d", pixel_w);
			LOGE(output_buffer);
			int res_update_texture = SDL_UpdateTexture(vedio_texture, NULL,
					buffer, pixel_w);
			int res_update_clear = SDL_RenderClear(renderer);
			int res_copy = SDL_RenderCopy(renderer, vedio_texture, NULL,
					play_win_rect);
			sprintf(output_buffer, "本帧视频绘制结果：更新材质：%d, 清除数据 :%d,复制数据到渲染器:%d",
					res_update_texture, res_update_clear, res_copy);
			LOGE(output_buffer);
			SDL_RenderPresent(renderer);
		}
	}
	LOGE("SDL退出");
	SDL_Quit();
	return 0;
}
/**
 * 初始化SDL视频部分参数
 */
jint Java_org_libsdl_app_VedioNativeHolder_initSDL(JNIEnv *env, jobject obj,
		jint vedio_width, jint vedio_height) {
	pixel_w = vedio_width;
	pixel_h = vedio_height;
}
/**
 * 初始化音频部分参数
 */
jint Java_org_libsdl_app_VedioNativeHolder_initAudioSDL(JNIEnv *env,
		jobject obj, jint audio_bitrate, jint audio_channels) {
	mAudioBitRate = audio_bitrate;
	mAudioChannels = audio_channels;
}

jint Java_org_libsdl_app_VedioNativeHolder_setVedioFrameData(JNIEnv *env,
		jobject obj, jbyteArray frameData) {
	if (renderer == NULL) {
		LOGE("renderer为NULL");
		return -1;
	}
	if (vedio_texture == NULL) {
		LOGE("Texture为NULL");
		return -1;
	}
	if (window == NULL) {
		LOGE("window为NULL");
		return -1;
	}
	if (frameData == NULL) {
		LOGE("视频数据为NULL");
		return 1;
	}
	LOGE("接收到数据进行绘制");
	buffer = (*env)->GetByteArrayElements(env, frameData,
	NULL);
	if (buffer == NULL) {
		LOGE("获取视频帧数据失败");
		return -1;
	} else {
		sprintf(output_buffer, "播放视频帧长度：%d",
				((*env)->GetArrayLength(env, frameData)));
		LOGE(output_buffer);
		SDL_Event event;
		event.type = REFRESH_EVENT;
		SDL_PushEvent(&event);
		LOGE("SDLEvent刷新界面");
	}
	return 0;
}

jint Java_org_libsdl_app_VedioNativeHolder_setAudioFrameData(JNIEnv *env,
		jobject obj, jbyteArray frameData) {

	if (frameData == NULL) {
		return -1;
	}
	if (isAudioInitSuccess == 0) {
		return -2;
	}
	audio_chunk = (uint8_t *) ((*env)->GetByteArrayElements(env, frameData,
	NULL));
	//Audio buffer length
	audio_len = 8192;
	audio_pos = audio_chunk;

	SDL_Event event;
	event.type = PLAY_AUDIO;
	SDL_PushEvent(&event);
	return 0;
}

void sdl_post_audio(){
	SDL_PauseAudio(0);
}
/**
 * 修改播放窗口大小的函数
 * 参数 左上点坐标 、 右上点坐标
 */
void Java_org_libsdl_app_VedioNativeHolder_changePlayWindow(JNIEnv *env,
		jobject obj, jint window_TLX, jint window_TLY, jint window_RBX,
		jint window_RBY) {
	play_win_rect->x = window_TLX;
	play_win_rect->y = window_TLY;
	play_win_rect->h = (window_RBY - window_TLY);
	play_win_rect->w = (window_RBX - window_TLX);
}

/**
 * 释放SDL资源使用的方法
 */
void Java_org_libsdl_app_VedioNativeHolder_quitSDL() {
	SDL_Event quit_event;
	quit_event.type = QUIT_EVENT;
	SDL_PushEvent(&quit_event);
}

