package com.cch.floatplugs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cch.floatplugs.FloatingDanmakuViewService.DanMakuFloatServiceIBinder;
import com.example.AndroidUtils.L;
import com.example.Utils.BiliDanmakuXMLParser;
import com.example.Utils.Constant;
import com.example.Utils.Constants;
import com.example.Utils.DataUtils;
import com.example.Utils.NetWorkUtils;
import com.example.customview.PopWinDanmakuSetting;
import com.example.javaBean.DanMaKu;
import com.example.javaBean.DanmuFileData;
import com.example.sdltestactivity.R;

public class FloatWinService extends Service{

	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mFloatWindowParams;
	private LayoutInflater mLayoutInflator;
	
	private View floatwin_contentView;
	private ImageButton imgbtn_menu;
	private LinearLayout ll_menu;
	private TextView tv_open_close_plugs;
	private TextView tv_plugs_time_setting;
	private TextView tv_danmaku_setting;
	private TextView tv_playing_status;
	private TextView tv_play_time;
	
	private VedioPlayListeningThread vedioListeningThread;
	private PopWinDanmakuSetting popwinSetting;

	private FloatingDanmakuViewService.DanMakuFloatServiceIBinder DMVFloatServiceBinder;
	private DanmakuFloatServiceConn danmakuFloatServiceConn;
	
	public Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if(msg.what == FloatConstants.AUDIO_PLAYING){
				tv_playing_status.setText("视频正在播放");
			}else if(msg.what == FloatConstants.AUDIO_STOP){
				tv_playing_status.setText("视频未在播放");
			}else if(msg.what == FloatConstants.VEDIO_TIME_UPDATE){
				long transVedioPlayTime = msg.getData().getLong("VedioPlayTime");
				tv_play_time.setText("视频播放时间:"+(int)(transVedioPlayTime / 100 / 60) +"分"+(int)(transVedioPlayTime / 100 % 60) + "秒"+(transVedioPlayTime % 100)+"毫秒");
			}
		}
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.e("tag","悬浮窗開啟");
		createFloatWin();
		Log.e("tag","悬浮窗插件初始化成功");
	}

	private void createFloatWin(){
		Log.e("tag","悬浮窗開啟");
		Log.e("tag","悬浮窗開啟");
		this.mLayoutInflator = LayoutInflater.from(getApplicationContext());
		//获取浮动窗口服务
		mWindowManager = (WindowManager) getSystemService(getApplication().WINDOW_SERVICE);
		//初始化浮动窗口参数
		mFloatWindowParams = new LayoutParams();
		mFloatWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		//设置浮动窗口基准方向为顶部和左边
		mFloatWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
		//设置浮动窗口大小
		mFloatWindowParams.x = 0;
		mFloatWindowParams.y = 150;
		mFloatWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mFloatWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		
		mFloatWindowParams.format = PixelFormat.RGBA_8888;
		//设置浮动窗口无法被聚焦，使得其下方控件可以响应事件
		mFloatWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		
		floatwin_contentView = mLayoutInflator.inflate(R.layout.floatwin_plus_button, null);
		floatwin_contentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

		imgbtn_menu = (ImageButton) floatwin_contentView.findViewById(R.id.btn_menu_open);
		ll_menu = (LinearLayout) floatwin_contentView.findViewById(R.id.ll_menu);
		tv_open_close_plugs = (TextView) floatwin_contentView.findViewById(R.id.tv_open_close_plugs);
		tv_plugs_time_setting = (TextView) floatwin_contentView.findViewById(R.id.tv_plugs_time_setting);
		tv_danmaku_setting = (TextView) floatwin_contentView.findViewById(R.id.tv_danmaku_setting);
		tv_playing_status = (TextView) floatwin_contentView.findViewById(R.id.tv_playing_status);
		tv_play_time = (TextView) floatwin_contentView.findViewById(R.id.tv_play_time);
		
		imgbtn_menu.setOnTouchListener(new OnFloatWinTouchListener());
		imgbtn_menu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(ll_menu.getVisibility() == View.GONE){
					ll_menu.setVisibility(View.VISIBLE);
				}else{
					ll_menu.setVisibility(View.GONE);
				}
			}
		});
		tv_open_close_plugs.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(DMVFloatServiceBinder.isDanmakuShown()){
					tv_open_close_plugs.setText("显示弹幕插件");
					DMVFloatServiceBinder.hideDanmakuPlay();
				}else{
					tv_open_close_plugs.setText("隐藏弹幕插件");
					DMVFloatServiceBinder.showDanmakuPlay();
				}
			}
		});
		
		tv_plugs_time_setting.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "时间重置",Toast.LENGTH_SHORT).show();
				vedioListeningThread.resetTimeCounting(0);
				DMVFloatServiceBinder.resetTime(0);
			}
		});
		
		tv_danmaku_setting.setOnClickListener(new OnClickListener() {
	
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			}
		});
		
		mWindowManager.addView(floatwin_contentView, mFloatWindowParams);
		NetGetDanmakuFileTaskTEST danmakuFileTaskTEST = new NetGetDanmakuFileTaskTEST(5059871);
		danmakuFileTaskTEST.execute();
		vedioListeningThread = new VedioPlayListeningThread(getApplicationContext(), handler); 
		vedioListeningThread.setTimeListener(new OnTimeChangeListener());
		Thread thread = new Thread(vedioListeningThread);
		thread.start();
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
        if(floatwin_contentView != null)
        {
            //移除悬浮窗口
            mWindowManager.removeView(floatwin_contentView);
        }
        vedioListeningThread.stopListen();
        if(danmakuFloatServiceConn != null){
        	unbindService(danmakuFloatServiceConn);
        }
	}
	
	//浮动窗口滑动事件
	class OnFloatWinTouchListener implements OnTouchListener{
		
		boolean isMove = false;
		int touchXInView = 0;
		int touchYInView = 0;
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				touchXInView = 0;
				touchYInView = 0;
				return false;
			}else if(event.getAction()== MotionEvent.ACTION_MOVE){
				if(isMove == false){
					touchXInView = (int) event.getX();
					touchYInView = (int) event.getY();
					isMove = true;
				}
				if(Math.abs(event.getRawX()) < 30 && Math.abs(event.getRawY()) < 30){
					Log.e("tag","移动距离过短");
					return false;
				}
				mFloatWindowParams.x = (int) event.getRawX() - touchXInView;
				mFloatWindowParams.y = (int) event.getRawY() - 25 - touchYInView;
                mWindowManager.updateViewLayout(floatwin_contentView, mFloatWindowParams);
				return true;
			}else if(event.getAction() == MotionEvent.ACTION_UP){
				boolean res = isMove;
				isMove = false;
				return res;
			}
			return false;
		}
	}
	
	/**
	 * 弹幕时间监听器
	 * @author 晨晖
	 *
	 */
	class OnTimeChangeListener implements VedioTimeListener{
		@Override
		public void onTimeChange(long curTimeMills) {
			if(FloatWinService.this.DMVFloatServiceBinder != null){
				//Log.e("FLoatWin",String.format("当前监听服务计算播放时间:%s", curTimeMills));
				FloatWinService.this.DMVFloatServiceBinder.freshTime(curTimeMills);
				Log.e("OnTimeChangeListener",String.format("弹幕时间驱动当前毫秒:%s", FloatWinService.this.DMVFloatServiceBinder.getCurTimeInMills()));
			}
		}
	}
	
	/**
	 * 弹幕悬浮窗服务与悬浮插件的连接类
	 * @author 晨晖
	 *
	 */
	class DanmakuFloatServiceConn implements ServiceConnection{

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			FloatWinService.this.DMVFloatServiceBinder = (DanMakuFloatServiceIBinder) service;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
		}
	}

	/**
     * 获取弹幕文件异步任务
     */
    class NetGetDanmakuFileTaskTEST extends AsyncTask<Void, Void, DanmuFileData> {
        private int chatId;

        private String resStr;

        public NetGetDanmakuFileTaskTEST(int chatId) {
            this.chatId = chatId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), String.format("正在获取弹幕文件:CID = %s", chatId), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected DanmuFileData doInBackground(Void... voids) {
            Boolean isSuccessed = false;

            try {
                isSuccessed = NetWorkUtils.downLoadDanmuFile(chatId);
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                return null;
            }
            L.e("网络请求成功" + isSuccessed);
            if (!isSuccessed) {
                return null;
            }
            //File danmuFile = new File("/sdcard/Download/3625120.xml");

            //暂时更新弹幕文件位置
            File danmuFile = new File(Constant.DANMU_FILE_SAVE_PATH + chatId);
            if (!danmuFile.exists()) {
                return null;
            }
            //暂时更新文件位置
            L.e("文件解析成功");
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(danmuFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                L.e("文件流读取失败");
                return null;
            }
            L.e("文件流读取成功");
            BiliDanmakuXMLParser biliDanmakuXMLParser = (BiliDanmakuXMLParser) DataUtils.parseXMLToDanmakuFile(fileInputStream, new BiliDanmakuXMLParser());
            return biliDanmakuXMLParser.getDanmakuFileData();
        }

        @Override
        protected void onPostExecute(DanmuFileData danmuFileData) {
            super.onPostExecute(danmuFileData);
            if (danmuFileData == null) {
            	  Toast.makeText(getApplicationContext(), "弹幕获取失败", Toast.LENGTH_SHORT).show();
            } else {
            	Toast.makeText(getApplicationContext(), "弹幕文件获取成功", Toast.LENGTH_SHORT).show();
                Log.e("tag", "弹幕服务器" + danmuFileData.getChatServer());
                Log.e("tag", "弹幕数量" + danmuFileData.getDanmuList().size());
                
                testMethod(danmuFileData.getDanmuList());
                //Constants.danmuList = getTestList();
                Collections.sort(danmuFileData.getDanmuList(), new SortByTimeDesc());
                //设置弹幕到全局变量处，准备播放
                //startService(new Intent(FloatWinService.this , FloatingDanmakuViewService.class));
                danmakuFloatServiceConn = new DanmakuFloatServiceConn();
                bindService(new Intent(FloatWinService.this , FloatingDanmakuViewService.class), danmakuFloatServiceConn,  Context.BIND_AUTO_CREATE);
            }
        }
	
        private List<DanMaKu> testMethod(List<DanMaKu> damakuList){
        	Constants.danmuList = new ArrayList<DanMaKu>();
        	for(int i = 0 ; i < 10 ; i++){
        		 for(int m = 0 ; m < damakuList.size() ; m++){
        			 DanMaKu tempDanmaku = new DanMaKu(damakuList.get(m));
        			 Constants.danmuList.add(tempDanmaku);
        		 }
        	}
			return Constants.danmuList;
        }

        class SortByTimeDesc implements Comparator<DanMaKu> {

            @Override
            public int compare(DanMaKu danMaKu, DanMaKu t1) {

                return danMaKu.getShowTime() < t1.getShowTime() ? -1 : 1;
            }
        }
    }
}
