package com.example.sdltestactivity;

import org.libsdl.app.VedioBaseInfo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.example.customview.VedioView;
import com.example.customview.VedioViewConstants;
import com.example.customview.Listener.VedioBufferListener;
import com.example.customview.Listener.VedioPlayStatusChangedListener;
import com.example.customview.Listener.VedioPlayTimeListener;

//        /storage/emulated/0/
public class StartActivity extends Activity {

	private SeekBar pb_vedio_progress;
	private LinearLayout ll_vedio_player_frame;
	private VedioView vedioPlayer;
	private TextView tv_play_time;
	private TextView tv_vedio_details;
	private Button btn_change_rate;
	private Button btn_play_vedio;

	private String transPlayFilePath;
	
	public static int HANDLER_TIME_UPDATE = 1;
	public static int HANDLER_CACHETIME_UPDATE = 2;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == HANDLER_TIME_UPDATE) {
				pb_vedio_progress.setProgress((int) (msg.getData().getLong(
						"curTime") * 100 / msg.getData().getLong("totalTime")));
				String strTime = (msg.getData().getLong("curTime")) + "/"
						+ ((int) msg.getData().getLong("totalTime"));
				tv_play_time.setText(strTime);
			}else if(msg.what == HANDLER_CACHETIME_UPDATE){
				pb_vedio_progress.setSecondaryProgress((int) (msg.getData().getLong(
						"cacheTime") * 100 / msg.getData().getLong("totalTime")));
				String strTime = (msg.getData().getLong("cacheTime")) + "/"
						+ ((int) msg.getData().getLong("totalTime"));
				tv_play_time.setText(strTime);
			}
		}
	};

	private void initView() {
		ll_vedio_player_frame = (LinearLayout) findViewById(R.id.ll_vedio_player_frame);
		pb_vedio_progress = (SeekBar) findViewById(R.id.pb_vedio_progress);
		tv_play_time = (TextView) findViewById(R.id.tv_time_lable);
		tv_vedio_details = (TextView) findViewById(R.id.tv_time_details);
		btn_change_rate = (Button) findViewById(R.id.btn_change_rate);
		btn_play_vedio = (Button) findViewById(R.id.btn_play_vedio);

		vedioPlayer = new VedioView(this);
		vedioPlayer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		ll_vedio_player_frame.addView(vedioPlayer);
		vedioPlayer.setVedioPlayTimeListener(new OnVedioTimeListener());
		vedioPlayer
				.setVedioPlayStatusChangedListener(new OnVedioChangeListener());
		vedioPlayer.setBufferListener(new VedioBufferListener() {
			
			@Override
			public void OnBuffering(double cacheRate) {
				// TODO Auto-generated method stub
				System.out.println("视频缓冲："+cacheRate);
			}
		});
		vedioPlayer.startVedioPlay(transPlayFilePath);

		pb_vedio_progress
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						vedioPlayer.seekVedio(vedioPlayer.getPlayingVedioDuration() / 100 * seekBar.getProgress());
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						// TODO Auto-generated method stub
					}
				});
		btn_change_rate.setOnClickListener(new OnChangeVedioPlayRate());
		btn_play_vedio.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (vedioPlayer.isVedioPlaying()) {
					btn_play_vedio.setText("播放");
					vedioPlayer.pauseVedioPlay();
				} else {
					btn_play_vedio.setText("暂停");
					vedioPlayer.resumeVedioPlay();
				}
			}
		});
	}

	private void initVedioDetail(VedioBaseInfo info) {
		if (info == null) {
			return;
		}
		String strVedioDetails = String.format("视频基本信息：\n视频宽："
				+ info.vedioWidth + " \n视频高：" + info.vedioHeight + " \n视频编码:"
				+ info.vedioCodecType + "\n比特率：" + info.bitRate + "\n帧率："
				+ info.vedio_fps + "\n事件基：" + info.vedio_time_base + "\n视频解码器："
				+ info.vedioCodecName);
		String str2 = String.format("音频比特率：%d \n 音频解析器：%s\n 声道数：%d\n声音采样率:%d",
				info.audioBitRate, info.audioCodecName, info.audioChannels,
				info.audioSampleRate);
		tv_vedio_details.setText(strVedioDetails + "\n" + str2);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		transPlayFilePath = getIntent().getStringExtra("FilePath");
		setContentView(R.layout.activity_start);
		Log.e("StartActivity", transPlayFilePath);
		initView();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			vedioPlayer.stopPlay();
			return true;
		}
		return super.onKeyDown(keyCode, event);
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
			Log.d("tag","视频时间刷新");
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
			Log.d("tag","缓冲时间刷新");
		}
	}

	class OnVedioChangeListener implements VedioPlayStatusChangedListener {

		@Override
		public void OnChanged(int status) {
			// TODO Auto-generated method stub
			if (status == VedioViewConstants.VP_STATUS_INIT_OK) {
				initVedioDetail(vedioPlayer.getPlayingVedioInfo());
			} else if (status == VedioViewConstants.VP_STATUS_PLAY_OVER) {
				Toast.makeText(StartActivity.this, "视频播放结束", Toast.LENGTH_LONG)
						.show();
			} else if (status == VedioViewConstants.VP_STATUS_VEDIO_PLAY_PAUSE) {
				Toast.makeText(StartActivity.this, "暂停播放", Toast.LENGTH_LONG)
						.show();
			} else if (status == VedioViewConstants.VP_STATUS_VEDIO_PLAY_RESUME) {
				Toast.makeText(StartActivity.this, "继续播放", Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	class OnChangeVedioPlayRate implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (vedioPlayer.getCurVedioRate() == VedioViewConstants.VEDIO_WIN_RATE.RATE_4_3) {
				vedioPlayer
						.changeVedioWinRate(VedioViewConstants.VEDIO_WIN_RATE.RATE_16_9);
				btn_change_rate.setText("16：9");
			} else if (vedioPlayer.getCurVedioRate() == VedioViewConstants.VEDIO_WIN_RATE.RATE_16_9) {
				vedioPlayer.changeVedioWinRate(VedioViewConstants.VEDIO_WIN_RATE.RATE_FULL);
				btn_change_rate.setText("FULL");
			} else if (vedioPlayer.getCurVedioRate() == VedioViewConstants.VEDIO_WIN_RATE.RATE_FULL) {
				vedioPlayer
						.changeVedioWinRate(VedioViewConstants.VEDIO_WIN_RATE.RATE_ORIGINAL_RATE);
				btn_change_rate.setText("原始");
			} else if (vedioPlayer.getCurVedioRate() == VedioViewConstants.VEDIO_WIN_RATE.RATE_ORIGINAL_RATE) {
				vedioPlayer
						.changeVedioWinRate(VedioViewConstants.VEDIO_WIN_RATE.RATE_4_3);
				btn_change_rate.setText("4：3");
			}
		}

	}
}
