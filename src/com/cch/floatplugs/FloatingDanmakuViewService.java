package com.cch.floatplugs;

import DanMakuClass.DanMaKuView;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.AndroidUtils.ScreenUtils;
import com.example.sdltestactivity.R;
import com.example.sdltestactivity.Interface.DanMaKuEnigineTimeDriver;

public class FloatingDanmakuViewService extends Service {

	private LinearLayout ll_danmaku_layer;
	private Context context;
	private LayoutInflater inflater;
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mWindowParams;
	private DanMaKuView dmkv_danmulayer;
	
	private int mScreenTrade = Configuration.ORIENTATION_PORTRAIT;
	
	private DanMakuFloatServiceIBinder danMakuFloatServiceIBinder; 
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		initService();
		Log.e("tag","service onBind");
		return danMakuFloatServiceIBinder;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.e("tag","service oncreate");
		//initService();
	}

	/**
	 * 初始化弹幕层悬浮窗服务
	 */
	private void initService(){
		mWindowManager = (WindowManager) getSystemService(getApplication().WINDOW_SERVICE);
		//初始化浮动窗口参数
		mWindowParams = new LayoutParams();
		mWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		//设置浮动窗口基准方向为顶部和左边
		mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
		//设置浮动窗口大小
		mWindowParams.x = 0;
		mWindowParams.y = 0;
		mWindowParams.height = ScreenUtils.getScreenHeight(getApplicationContext());
		mWindowParams.width = ScreenUtils.getScreenWidth(getApplicationContext());
		
		mWindowParams.format = PixelFormat.RGBA_8888;
		//设置浮动窗口无法被聚焦，使得其下方控件可以响应事件
		mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
				
		inflater = LayoutInflater.from(getApplicationContext());
		ll_danmaku_layer = (LinearLayout) inflater.inflate(R.layout.floatwin_danmaku_layer, null);
		ll_danmaku_layer.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		dmkv_danmulayer = new DanMaKuView(getApplication());
		
		
		ll_danmaku_layer.addView(dmkv_danmulayer);
		dmkv_danmulayer.resume();
		mWindowManager.addView(ll_danmaku_layer, mWindowParams);
		danMakuFloatServiceIBinder = new DanMakuFloatServiceIBinder();
		dmkv_danmulayer.setDanMaKuEnigineTimeDriver(danMakuFloatServiceIBinder);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
        if(ll_danmaku_layer != null)
        {
            //移除悬浮窗口
            mWindowManager.removeView(ll_danmaku_layer);
        }
	}

	/**
	 * 手机屏幕旋转监听器,监听旋转状态,动态改变弹幕窗口宽度
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		  if(newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){  
	            Toast.makeText(FloatingDanmakuViewService.this, "现在是竖屏", Toast.LENGTH_SHORT).show();  
	        }  
	        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){  
	            Toast.makeText(FloatingDanmakuViewService.this, "现在是横屏", Toast.LENGTH_SHORT).show();  
	        }   
		//屏幕方向发生旋转
		 if(newConfig.orientation != mScreenTrade){  
			 dmkv_danmulayer.clearAllDanMaku();
			 //获取屏幕宽高
			 int screenWidth = ScreenUtils.getScreenWidthInService(getApplication());
			 int screemHeight = ScreenUtils.getScreenHeightInService(getApplication());
			 mScreenTrade = newConfig.orientation;
			 //重置弹幕播放控件尺寸
			 dmkv_danmulayer.resetDanmakuViewSize(screemHeight , screenWidth);
	     }  
	}
	
	/**
	 * 弹幕悬浮层控制Binder接口
	 * @author 晨晖
	 *
	 */
	class DanMakuFloatServiceIBinder extends Binder implements DanMaKuEnigineTimeDriver{
		
		private long curDanmakuPlayTimeMills = 0;
		
		//更新弹幕播放器时间
		protected void freshTime(long curMills){
			this.curDanmakuPlayTimeMills = curMills;
		}

		public void pauseDanmakuPlay(){
			dmkv_danmulayer.pause();
		}
		
		public void resumeDnamakuPlay(){
			dmkv_danmulayer.resume();
		}
		
		public void resetTime(long timeMills){
			this.curDanmakuPlayTimeMills = timeMills;
			dmkv_danmulayer.setAllplayTime(timeMills);
		}
		public void hideDanmakuPlay(){
			dmkv_danmulayer.setVisibility(View.GONE);
		}

		public void showDanmakuPlay(){
			dmkv_danmulayer.setVisibility(View.VISIBLE);
		}
		
		public boolean isDanmakuShown(){
			return dmkv_danmulayer.isShown();
		}
		
		public void clearAllPlayingDanmaku(){
			dmkv_danmulayer.clearAllDanMaku();
		}
		
		@Override
		public long getCurTimeInMills() {
			return this.curDanmakuPlayTimeMills;
		}
	}
}
