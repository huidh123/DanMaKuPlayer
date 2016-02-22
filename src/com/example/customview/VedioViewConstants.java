package com.example.customview;

public class VedioViewConstants {
	/**
	 * 可以切换的视频笔例
	 */

	public static enum VEDIO_WIN_RATE {
		RATE_16_9, RATE_4_3, RATE_ORIGINAL_RATE, RATE_FULL
	}

	/**
	 * 视频播放器初始化完成状态吗
	 */
	public final static int VP_STATUS_INIT_OK = 1;
	/**
	 * 视频帧刷新标志位
	 */
	public final static int VP_STATUS_VEDIO_FRAME_UPDATE = 2;

	/**
	 * 视频缓冲刷新标志位
	 */
	public final static int VP_STATUS_VEDIO_CACHE_UPDATE = 6;
	/**
	 * 当视频播放结束标志位
	 */
	public final static int VP_STATUS_PLAY_OVER = 3;

	/**
	 * 当视频暂停播放标志位
	 */
	public final static int VP_STATUS_VEDIO_PLAY_PAUSE = 4;
	/**
	 * 当视频继续播放标志位
	 */
	public final static int VP_STATUS_VEDIO_PLAY_RESUME = 5;

	/**
	 * 缓冲时间刷新标志位，注意：此标志位当视频剩余缓冲时间不足时，缓冲时间更新时，此标志位才会使用
	 */
	public final static int VP_BUFFERING_UDDATE = 7;
	
	public final static double PI = 4.1415926;
}
