package com.example.sdltestactivity;

import DanMakuClass.DanMaKuView;
import DanMakuClass.DanMaKuViewConstants;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Utils.Constants;
import com.example.customview.BufferingPopupWindow;
import com.example.customview.PopWinDanmakuSetting;
import com.example.customview.VedioPlayGestureControl;
import com.example.customview.VedioView;
import com.example.customview.VedioViewConstants;
import com.example.customview.Listener.VedioBufferListener;
import com.example.customview.Listener.VedioPlayStatusChangedListener;
import com.example.customview.Listener.VedioPlayTimeListener;
import com.example.javaBean.DanMaKu;
import com.example.sdltestactivity.Interface.DanMaKuEnigineTimeDriver;
import com.example.sdltestactivity.Interface.VedioControlInterface;
import com.example.sdltestactivity.Interface.VedioPlayStateInterface;

public class GiliGiliPlayActivity extends Activity implements
		DanMaKuEnigineTimeDriver, VedioControlInterface,
		VedioPlayStateInterface{

	private RelativeLayout rl_content;
	private VedioView vv_video_player;
	private TextView tv_play_time;
	private ImageButton btn_pause;
	private SeekBar sb_player_progress;
	private LinearLayout ll_video_controler;
	private BufferingPopupWindow bufferingPopupWindow;
	private DanMaKuView dmkv_player;
	private LinearLayout ll_danmaku_view;
	private ImageButton btn_start;
	private RelativeLayout rl_vedioPlay;
	private ImageButton btn_menu;
	private Button btn_1;
	private Button btn_2;
	private Button btn_3;
	private Button btn_4;
	private Button btn_5;
	
	
	private PopWinDanmakuSetting popwin_danmakuSetting;
	private int Layout_Mode = 0;
	
	private int videoDuration;
	private int videoCurPlayTime;
	private GestureDetector mGestureDetector;
	private String transPlayVedioPath;
	
	private AudioManager mAudioManager;
	// 常量
	private final String tag = "VideoPlayerActivity";
	private final static int HIDE_DELAY_TIME = 3000;
	public final static int HANDLER_TIME_UPDATE = 1;
	public final static int HANDLER_CACHETIME_UPDATE = 2;
	public final static int HANDLER_BUFFERING = 5;
	public final static int HANDLER_BUFFERD_OK = 7;
	public final static int HIDE_CONTROLER = 3;
	public final static int PLAYER_INIT_OK = 4;
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == HANDLER_TIME_UPDATE) {
				sb_player_progress.setProgress((int) (msg.getData().getLong("curTime")));
				String strTime = (msg.getData().getLong("curTime") / 1000) + "/"
						+ ((int) msg.getData().getLong("totalTime") / 1000);
				tv_play_time.setText(strTime);
			}else if(msg.what == HANDLER_CACHETIME_UPDATE){
				sb_player_progress.setSecondaryProgress((int) (msg.getData().getLong("cacheTime")));
			}else if(msg.what == HIDE_CONTROLER){
                ll_video_controler.setVisibility(View.INVISIBLE);
            }else if(msg.what == PLAYER_INIT_OK){
            	Log.e("max duration","视频时长："+(int)(msg.getData().getLong("totalTime")));
            	sb_player_progress.setMax((int)(msg.getData().getLong("totalTime")));
            }else if(msg.what == HANDLER_BUFFERING){
            	if(!bufferingPopupWindow.isShowing()){
            		bufferingPopupWindow.showAtLocation(ll_video_controler, Gravity.CENTER, 0, 0);
            	}
            	bufferingPopupWindow.setProgressPercent((int)(msg.getData().getDouble("cacheRate")*100));
            }else if(msg.what == HANDLER_BUFFERD_OK){
            	bufferingPopupWindow.dismiss();
            }else if(msg.what == 10000){
            	dmkv_player.start();
            }
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		transPlayVedioPath = getIntent().getStringExtra("VedioPath");
		initScreenLayout();
		setContentView(R.layout.activity_video_play_layout);
		initViews();
	}

	/**
	 * 初始化Activty设置，例如全屏和屏幕常亮
	 */
	private void initScreenLayout() {
		// 隐藏标题
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 设置全屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//设置屏幕常亮
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	private void initViews() {
		rl_content = (RelativeLayout) findViewById(R.id.rl_content);
		ll_danmaku_view = (LinearLayout) findViewById(R.id.ll_danmaku_view);
		btn_pause = (ImageButton) findViewById(R.id.btn_pause);
		ll_video_controler = (LinearLayout) findViewById(R.id.ll_video_controler);
		sb_player_progress = (SeekBar) findViewById(R.id.sb_player_progress);
		tv_play_time = (TextView) findViewById(R.id.tv_play_time);
		btn_start = (ImageButton) findViewById(R.id.btn_start);
		rl_vedioPlay = (RelativeLayout) findViewById(R.id.rl_vedioPlay);
		btn_1 = (Button) findViewById(R.id.btn_1);
		btn_2 = (Button) findViewById(R.id.btn_2);
		btn_3 = (Button) findViewById(R.id.btn_3);
		btn_4 = (Button) findViewById(R.id.btn_4);
		btn_5 = (Button) findViewById(R.id.btn_5);
		btn_menu = (ImageButton) findViewById(R.id.btn_menu);
		dmkv_player = (DanMaKuView) findViewById(R.id.dmkv_player);

		vv_video_player = new VedioView(this);
		vv_video_player.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		rl_vedioPlay.addView(vv_video_player);
		// 添加弹幕时间驱动
		dmkv_player.setDanMaKuEnigineTimeDriver(this);
		//设置过滤器
//		dmkv_player.getDanmakuManager().addDanmakuFliter(new AmountFliter());;
//		GoodWordFliter fliter= new GoodWordFliter("洛阳铲");
//		dmkv_player.getDanmakuManager().addDanmakuFliter(fliter);
		
		popwin_danmakuSetting = new PopWinDanmakuSetting(this , dmkv_player);
		
		new Thread(){
			@Override
			public void run() {
				super.run();
				dmkv_player.setPlayDanMaKuList(Constants.danmuList);
				handler.sendEmptyMessage(10000);
			}
		}.start();
		
		VedioPlayGestureControl vedioPlayGestureControl = new VedioPlayGestureControl(
				GiliGiliPlayActivity.this, this, this);
		mGestureDetector = new GestureDetector(getApplicationContext(),vedioPlayGestureControl);
		sb_player_progress
				.setOnSeekBarChangeListener(new VideoSeekBarOnSeekListener());
		vv_video_player.setVedioPlayTimeListener(new OnVedioTimeListener());
		vv_video_player.setVedioPlayStatusChangedListener(new OnVedioChangeListener());
		vv_video_player.setBufferListener(new OnVedioBufferingListener());
		vv_video_player.startVedioPlay(transPlayVedioPath);
		btn_pause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (vv_video_player.isVedioPlaying()) {
					vv_video_player.pauseVedioPlay();
				} else {
					vv_video_player.resumeVedioPlay();;
					vv_video_player.requestFocus();
				}
			}
		});
		
		btn_menu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				popwin_danmakuSetting.showAtLocation(rl_content, Gravity.CENTER, 0, 0);
			}
		});
		
		
		bufferingPopupWindow = new BufferingPopupWindow(this);
		//定时关闭控制栏
		handler.sendEmptyMessageDelayed(HIDE_CONTROLER, HIDE_DELAY_TIME * 2);
		
		btn_1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DanMaKu tempDanmaku = new DanMaKu();
				tempDanmaku.setDanmakuContent("测试横向弹幕" + dmkv_player.getAllplayTime());
				tempDanmaku.setDanmakuTextSize(40);
				tempDanmaku.setDanmuColor("#FFFFFF");
				tempDanmaku.setDanmuType(DanMaKuViewConstants.DANMU_TYPE_RIGHT_TOLEFT);
				tempDanmaku.setShowTime(0);
				dmkv_player.addDanmaku(tempDanmaku, true);
			}
		});
		
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);;
		
	}

	/**
	 * 进度条事件监听器
	 */
	class VideoSeekBarOnSeekListener implements SeekBar.OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
			if (b == true) {
				seekBar.setProgress(i);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			vv_video_player.seekVedio(seekBar.getProgress());
			dmkv_player.setAllplayTime(seekBar.getProgress());
			Log.e("VedioControl","跳转");
		}
	}

	/**
	 * 手势控制类，控制对于主界面的手势操作
	 */
	class VideoActivityGestureListener extends
			GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (vv_video_player.isVedioPlaying()) {
				pauseVideoPlay();
			} else {
				startVideoPlay();
				vv_video_player.requestFocus();
			}
			return super.onDoubleTap(e);
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			if (ll_video_controler.isShown()) {
				ll_video_controler.setVisibility(View.INVISIBLE);
				handler.removeMessages(HIDE_CONTROLER);
			} else {
				ll_video_controler.setVisibility(View.VISIBLE);
				handler.sendEmptyMessageDelayed(HIDE_CONTROLER, HIDE_DELAY_TIME);
			}
			return super.onSingleTapUp(e);
		}
	}

	class OnVedioBufferingListener implements VedioBufferListener{
		@Override
		public void OnBuffering(double cacheRate) {
			//当缓冲比例不足时显示缓冲框
			if(cacheRate <= 0.95f){
				Message message = new Message();
				message.what = HANDLER_BUFFERING;
				Bundle bundle = new Bundle();
				bundle.putDouble("cacheRate", cacheRate);
				message.setData(bundle);
				handler.sendMessage(message);
			}else{
				handler.sendEmptyMessage(HANDLER_BUFFERD_OK);
			}
		}
	}
	class OnVedioChangeListener implements VedioPlayStatusChangedListener {
		@Override
		public void OnChanged(int status) {
			// TODO Auto-generated method stub
			if (status == VedioViewConstants.VP_STATUS_INIT_OK) {
				Toast.makeText(GiliGiliPlayActivity.this, "初始化完成", Toast.LENGTH_LONG)
				.show();
				Message msg = new Message();
				msg.what = PLAYER_INIT_OK;
				Bundle bundle = new Bundle();
				bundle.putLong("totalTime", vv_video_player.getPlayingVedioDuration());
				msg.setData(bundle);
				handler.sendMessage(msg);
			} else if (status == VedioViewConstants.VP_STATUS_PLAY_OVER) {
				Toast.makeText(GiliGiliPlayActivity.this, "视频播放结束", Toast.LENGTH_LONG)
						.show();
			} else if (status == VedioViewConstants.VP_STATUS_VEDIO_PLAY_PAUSE) {
				Toast.makeText(GiliGiliPlayActivity.this, "暂停播放", Toast.LENGTH_LONG)
						.show();
				dmkv_player.pause();
			} else if (status == VedioViewConstants.VP_STATUS_VEDIO_PLAY_RESUME) {
				Toast.makeText(GiliGiliPlayActivity.this, "继续播放", Toast.LENGTH_LONG)
						.show();
				dmkv_player.resume();
			}
		}
	}
	
	class OnVedioTimeListener implements VedioPlayTimeListener {

		@Override
		public void onTime(long curTime, long totalTime) {
			Message message = new Message();
			message.what = HANDLER_TIME_UPDATE;
			Bundle bundle = new Bundle();
			bundle.putLong("curTime", curTime);
			bundle.putLong("totalTime", totalTime);
			message.setData(bundle);
			handler.sendMessage(message);
		}

		@Override
		public void onCacheTime(long cachTime, long totalTime) {
			Message message = new Message();
			message.what = HANDLER_CACHETIME_UPDATE;
			Bundle bundle = new Bundle();
			bundle.putLong("totalTime", totalTime);
			bundle.putLong("cacheTime", cachTime);
			message.setData(bundle);
			handler.sendMessage(message);
		}
	}
	@Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			dmkv_player.stop();
			vv_video_player.stopPlay();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean getIsControlViewShown() {
		return ll_video_controler.isShown();
	}

	@Override
	public boolean getIsVideoPlaying() {
		return vv_video_player.isVedioPlaying();
	}

	@Override
	public void startVideoPlay() {
		vv_video_player.resumeVedioPlay();
	}

	@Override
	public void pauseVideoPlay() {
		vv_video_player.pauseVedioPlay();
	}

	@Override
	public void movePlayDuration(int deltaMills) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeVolume(int deltaVolume) {
		Log.e("tag","修改音量");
		if(deltaVolume < 0){
			mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,AudioManager.FX_FOCUS_NAVIGATION_UP);    
		}else{
			mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE,AudioManager.FX_FOCUS_NAVIGATION_UP);    
		}
		
	}

	@Override
	public void changeBrightness(int deltaCount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void isShowControlView(boolean isShow) {
		 if(!isShow){
	            ll_video_controler.setVisibility(View.INVISIBLE);
	            handler.removeMessages(HIDE_CONTROLER);
	       }else{
	            ll_video_controler.setVisibility(View.VISIBLE);
	            handler.sendEmptyMessageDelayed(HIDE_CONTROLER, HIDE_DELAY_TIME);
	       } 
	}

	@Override
	public void showStatePopWin(PopupWindow window) {

	}

	@Override
	public long getCurTimeInMills() {
		return vv_video_player.getCurPlayTime();
	}
	
	
}
