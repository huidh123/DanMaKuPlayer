package com.example.customview.Listener;

public interface VedioPlayTimeListener {
	
	public void onTime(long curTime , long totalTime);
	public void onCacheTime(long cachTime , long totalTime);
}
