package com.example.customview;

import java.io.UnsupportedEncodingException;

import org.libsdl.app.SDLActivity;
import org.libsdl.app.VedioBaseInfo;
import org.libsdl.app.VedioFrameInfo;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import com.example.customview.VedioViewConstants.VEDIO_WIN_RATE;
import com.example.customview.Listener.VedioBufferListener;
import com.example.customview.Listener.VedioPlayStatusChangedListener;
import com.example.customview.Listener.VedioPlayTimeListener;
public class VedioView extends LinearLayout {
	
	static {
//		System.loadLibrary("SDL2");
//		System.loadLibrary("SDLVedio");
	}
	
	private SDLActivity mPlayer;

	private static String tag = "VedioView";
	private int freshTimeMills = 0;
	private static boolean mIsPlaying = true;
	private float vedioPlatSpeedRate = 1.0f;

	//用于保存播放播放视频信息
	private static VedioBaseInfo playingVedioBaseInfo;
	private static VedioFrameInfo vedioFrameInfo;
	
	private VEDIO_WIN_RATE mCurVedioPlayRate = VEDIO_WIN_RATE.RATE_FULL;

	private long curPlayTime = 0;
	private static VedioPlayTimeListener vedioPlayTimeListener = new VedioPlayTimeListener() {
		@Override
		public void onTime(long curTime, long totalTime) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onCacheTime(long cachTime, long totalTime) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private static VedioPlayStatusChangedListener changedListener = new VedioPlayStatusChangedListener() {
		@Override
		public void OnChanged(int status) {
		}
	};
	
	private static VedioBufferListener bufferListener =new VedioBufferListener() {
		
		@Override
		public void OnBuffering(double cacheRate) {
		}
	};
	
	private static Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			// 当初始化成功之后，可以获取基础信息
			if (msg.what == VedioViewConstants.VP_STATUS_INIT_OK) {
				playingVedioBaseInfo = VedioView.this.getOpenedFileInfo();
				VedioView.this.changedListener
						.OnChanged(VedioViewConstants.VP_STATUS_INIT_OK);
			} else if (msg.what == VedioViewConstants.VP_STATUS_VEDIO_FRAME_UPDATE) {
				vedioPlayTimeListener.onTime(VedioView.this.getCurVedioPlayTime(),
						playingVedioBaseInfo.vedioTimeLength );
			}else if(msg.what == VedioViewConstants.VP_STATUS_VEDIO_CACHE_UPDATE){
				if(playingVedioBaseInfo.vedioTimeLength != 0){
					vedioPlayTimeListener.onCacheTime((long) VedioView.this.getCacheVedioTime(),
							playingVedioBaseInfo.vedioTimeLength );
					VedioView.this.bufferListener.OnBuffering(getCacheVedioFrameRate());
				}
			}else if (msg.what == VedioViewConstants.VP_STATUS_PLAY_OVER) {
				VedioView.this.changedListener
						.OnChanged(VedioViewConstants.VP_STATUS_PLAY_OVER);
			}else if(msg.what == VedioViewConstants.VP_STATUS_VEDIO_PLAY_PAUSE){
				VedioView.this.changedListener.OnChanged(VedioViewConstants.VP_STATUS_VEDIO_PLAY_PAUSE);
				mIsPlaying =  false;
			}else if(msg.what == VedioViewConstants.VP_STATUS_VEDIO_PLAY_RESUME){
				VedioView.this.changedListener.OnChanged(VedioViewConstants.VP_STATUS_VEDIO_PLAY_RESUME);
				mIsPlaying =  true;
			}
			else if(msg.what == VedioViewConstants.VP_BUFFERING_UDDATE){
				VedioView.this.bufferListener.OnBuffering(getCacheVedioFrameRate());
			}
		}
	};

	public VedioView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		initLayout(context);
	}

	public VedioView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initLayout(context);
	}

	public VedioView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initLayout(context);
	}

/** 视频控制部分代码 **/
	public void startVedioPlay(String vedioFilePath) {
		int res = -1;
		try {
			res = initVedioPlayer(new String(vedioFilePath.getBytes(), "gb2312"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (res != 0) {
			Log.e(tag, "解码器初始化失败，错误代码：" + res);
			return;
		}
	}

	/**
	 * 停止播放
	 */
	public void stopPlay() {
		nativeStopPlay();
		//还需要回收内存
	}

	/**
	 * 视频暂停播放
	 */
	public void pauseVedioPlay() {
		this.nativePauseVedio();
	}

	/**
	 * 继续视频播放
	 */
	public void resumeVedioPlay() {
		this.nativeResumeVedio();
	}

	/**
	 * 设置视频播放的速度倍率
	 * 
	 * @param rate
	 *            视频播放速度倍率 [1.0f - 8.0f]
	 */
	public void setPlaySpeedRate(float rate) {
		if (rate <= 1) {
			Log.e(tag, "视频播放速度倍率过小");
			vedioPlatSpeedRate = 1;
		} else if (rate >= 8) {
			Log.e(tag, "视频播放速度倍率过大");
			vedioPlatSpeedRate = 8;
		} else {
			vedioPlatSpeedRate = rate;
		}
	}

	/**
	 * 修改视频播放比例
	 * 
	 * @param vedioRate
	 */
	public void changeVedioWinRate(VEDIO_WIN_RATE vedioRate) {
		mCurVedioPlayRate = vedioRate;
		int vedio_veiw_height = this.getMeasuredHeight();
		int vedio_veiw_width = this.getMeasuredWidth();
		int vedio_height = this.playingVedioBaseInfo.vedioHeight;
		int vedio_width = this.playingVedioBaseInfo.vedioWidth;

		if (vedioRate == VEDIO_WIN_RATE.RATE_4_3) {
			int play_height = vedio_veiw_height;
			int play_width = (play_height / 3) * 4;
			changePlayWindow((vedio_veiw_width - play_width) / 2,
					0, (vedio_veiw_width - play_width) / 2 + play_width,
					play_height);
		} else if (vedioRate == VEDIO_WIN_RATE.RATE_16_9) {
			int play_width = vedio_veiw_width;
			int play_height = vedio_veiw_width / 16 * 9;
			changePlayWindow(0,
					(vedio_veiw_height - play_height) / 2, vedio_veiw_width,
					(vedio_veiw_height - play_height) / 2 + play_height);
		} else if (vedioRate == VEDIO_WIN_RATE.RATE_ORIGINAL_RATE) {
			float rate_X = vedio_veiw_width / vedio_width;
			float rate_Y = vedio_veiw_height / vedio_height;
			int play_height = 0;
			int play_width = 0;
			if (rate_X > rate_Y) {
				play_height = (int) (vedio_height * rate_X);
				play_width = (int) (vedio_width * rate_X);
				changePlayWindow(
						(vedio_veiw_width - play_width) / 2, 0,
						(vedio_veiw_width - play_width) / 2 + play_width,
						play_height);
			} else {
				play_height = (int) (vedio_height * rate_Y);
				play_width = (int) (vedio_width * rate_Y);
				changePlayWindow(0,
						(vedio_veiw_height - play_height) / 2,
						vedio_veiw_width, (vedio_veiw_height - play_height) / 2
								+ play_height);
			}
		} else if (vedioRate == VEDIO_WIN_RATE.RATE_FULL) {
			changePlayWindow(0, 0, vedio_veiw_width,
					vedio_veiw_height);
		}
	}
	
	/**
	 * 设置视频播放器时间监听器，注意这个时间监听器onTime（）会在子线程中被调用
	 * 
	 * @param vedioPlayTimeListener
	 */
	public void setVedioPlayTimeListener(VedioPlayTimeListener vedioPlayTimeListener) {
		this.vedioPlayTimeListener = vedioPlayTimeListener;
	}

	public void setVedioPlayStatusChangedListener( VedioPlayStatusChangedListener changedListener) {
		this.changedListener = changedListener;
	}
	
	public void setBufferListener( VedioBufferListener bufferListener) {
		VedioView.bufferListener = bufferListener;
	}


	public void seekVedio(long seekTime) {
		//重置当前时间
		curPlayTime = seekTime;
		seekVedioFrame(seekTime);
	}

	/**
	 * 用于初始化视频播放器的方法
	 * 
	 * @param context
	 */
	private void initLayout(Context context) {
		// 初始化JNI方法，帮助C层数据调用
		mPlayer = new SDLActivity(context);
		// 初始化音视频帧缓冲池
		this.addView(mPlayer.getSurfaceView());
	}
	/**
	 * 获取视频长度
	 * 
	 * @return
	 */
	public long getPlayingVedioDuration() {
		if (playingVedioBaseInfo != null) {
			return playingVedioBaseInfo.vedioTimeLength;
		}
		return 0;
	}

	/**
	 * 获取视频高度
	 * 
	 * @return
	 */
	public int getPlayingVedioHeight() {
		if (playingVedioBaseInfo != null) {
			return playingVedioBaseInfo.vedioHeight;
		}
		return 0;
	}
	
	/**
	 * 获取视频宽度
	 * 
	 * @return
	 */
	public int getPlayingVedioWidth() {
		if (playingVedioBaseInfo != null) {
			return playingVedioBaseInfo.vedioWidth;
		}
		return 0;
	}

	/**
	 * 获取视频基本信息
	 * 
	 * @return
	 */
	public VedioBaseInfo getPlayingVedioInfo() {
		return playingVedioBaseInfo;
	}

	/**
	 * 获取当前播放时间
	 * 
	 * @return
	 */
	public long getCurPlayTime() {
		long tempCurTime = getCurVedioPlayTime();
		if(curPlayTime <= tempCurTime){
			curPlayTime = tempCurTime;
		}else{
			Log.e("vedio play time",String.format("视频时间出现错误,获得时间：%d , 现在时间：%d", tempCurTime,curPlayTime));
		}
		return  curPlayTime;
	}
	/**
	 * 获取当前视频播放比例
	 * 
	 * @return
	 */
	public VEDIO_WIN_RATE getCurVedioRate() {
		return mCurVedioPlayRate;
	}

	/**
	 * 获取当前视频播放的速度倍率
	 * 
	 * @return
	 */
	public float getCurVedioPlaySpeedRate() {
		return vedioPlatSpeedRate;
	}

	/**
	 * 获取是否正在播放视频
	 * @return
	 */
	public boolean isVedioPlaying(){
		return mIsPlaying;
	}
	/**
	 * 视频播放器Java层接口，C层在完成工作之后会直接调用此方法
	 * 
	 * @param status
	 *            VedioViewConstants中保存
	 */
	protected static void NDK_C_OnVedioPlayerStateChanged(int status) {
		//Log.e(tag, "接收到數據" + status);
		handler.sendEmptyMessage(status);
		
	}

	// C层本地方法接口
	private static native int initVedioPlayer(String filepath);

	private static native VedioBaseInfo getOpenedFileInfo();

	private static native void nativePauseVedio();

	private static native void nativeResumeVedio();
	
	private static native void changePlayWindow(int pTLX , int pTLY , int pRBX , int pRBY);
	
	private static native void seekVedioFrame(long seek_time);
	
	private static native long getCurVedioPlayTime();
	
	private static native double getCacheVedioTime();
	
	private static native void nativeStopPlay();
	
	private static native double getCacheVedioFrameRate();
	
}
