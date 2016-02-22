package com.example.AndroidUtils;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

public class BrightnessUtils {
	private Context context;
	public BrightnessUtils(Context context){
		this.context = context;
	}
	private int getScreenMode() {
	    int screenMode = 0;
	    try {
	        screenMode = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
	    } catch (Exception localException) {

	    }
	    return screenMode;
	}

	/**
	 * 设置当前屏幕亮度的模式 SCREEN_BRIGHTNESS_MODE_AUTOMATIC=1 为自动调节屏幕亮度
	 * SCREEN_BRIGHTNESS_MODE_MANUAL=0 为手动调节屏幕亮度
	 */
	private void setScreenMode(int paramInt) {
	    try {
	        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, paramInt);
	    } catch (Exception localException) {
	        localException.printStackTrace();
	    }
	}

	/**
	 * 获得当前屏幕亮度值 0--255
	 */
	private int getScreenBrightness() {
	    int screenBrightness = 255;
	    try {
	        screenBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
	    } catch (Exception localException) {

	    }
	    return screenBrightness;
	}

	/**
	 * 设置当前屏幕亮度值 0--255
	 */
	private void saveScreenBrightness(int paramInt) {
	    try {
	        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, paramInt);
	    } catch (Exception localException) {
	        localException.printStackTrace();
	    }
	}

	/**
	 * 保存当前的屏幕亮度值，并使之生效
	 */
	private void setScreenBrightness(int paramInt) {
	    Window localWindow = ((Activity)context).getWindow();
	    WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();
	    float f = paramInt / 255.0F;
	    localLayoutParams.screenBrightness = f;
	    localWindow.setAttributes(localLayoutParams);
	}
}
