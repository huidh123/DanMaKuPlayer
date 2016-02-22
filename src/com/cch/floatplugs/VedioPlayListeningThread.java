package com.cch.floatplugs;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;


public class VedioPlayListeningThread implements Runnable {

	private Handler handler;
	private AudioManager mAudioManager;
	private Context mContext;
	private VedioPlayTimeCountThread countThread;
	
	private Bundle transBundle;
	private boolean isExit = false;
	boolean isLastPlaying = false;
	
	private VedioTimeListener timeListener = new VedioTimeListener() {
		
		@Override
		public void onTimeChange(long curTimeMills) {
		}
	};
	
	public VedioPlayListeningThread(Context context , Handler handler) {
		this.handler = handler;
		this.mContext = context;
		mAudioManager =  (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);  
		countThread = new VedioPlayTimeCountThread();
		transBundle = new Bundle();
	}
	@Override
	public void run() {
		countThread.start();
		while(isExit == false){
			//当播放状态未发生变化时
			if(isLastPlaying == mAudioManager.isMusicActive()){
				continue;
			}else{
				isLastPlaying = mAudioManager.isMusicActive();
				if(isLastPlaying){
					countThread.resumeListen();
					handler.sendEmptyMessage(FloatConstants.AUDIO_PLAYING);
				}else{
					countThread.pauseListen();
					handler.sendEmptyMessage(FloatConstants.AUDIO_STOP);
				}
			}
		}
	}
	
	/**
	 * 设置时间监听器
	 * @param timeListener
	 */
	public void setTimeListener (VedioTimeListener timeListener){
		this.timeListener = timeListener;
	}
	
	/**
	 * 获取当前计时线程播放时间
	 * @return
	 */
	public long getCurPlayTime(){
		return countThread.getCurVedioPlayTime();
	}
	
	/**
	 * 停止音频播放监听
	 */
	public void stopListen(){
		isExit = true;
	}
	
	public void resetTimeCounting(long timeMills){
		countThread.resetListen(timeMills);
	}
	/**
	 * 视频播放计时线程
	 * @author 晨晖
	 *
	 */
	class VedioPlayTimeCountThread extends Thread{
		private long startCountTime;
		public long totalPlayTime = 0;
		boolean isListen = false;
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			startCountTime = System.currentTimeMillis();
			while(isExit == false){
				if(isListen == false){
					continue;
				}
				if(0 != (System.currentTimeMillis() - startCountTime)){
					//添加时间差
					totalPlayTime += (System.currentTimeMillis() - startCountTime);
					startCountTime = System.currentTimeMillis();
					timeListener.onTimeChange(totalPlayTime);
				}
				try {
					sleep(5);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		public void pauseListen(){
			isListen = false;
		}
		
		public void resumeListen(){
			startCountTime = System.currentTimeMillis();
			isListen = true;
		}
		
		public void resetListen(long timeMills){
			startCountTime = System.currentTimeMillis();
			totalPlayTime = timeMills;
		}
		//返回当前计时线程累计时间
		public long getCurVedioPlayTime(){
			return this.totalPlayTime;
		}
	}
}
