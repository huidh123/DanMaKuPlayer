package com.cch.floatplugs;

/**
 * 用户监听视频时间变化的监听器，注意这个监听器是运行在非主线程中的
 * @author 晨晖
 *
 */
public interface VedioTimeListener {
	public void onTimeChange(long curTimeMills);
}
