package com.example.javaBean;

import java.util.ArrayList;

import android.graphics.Bitmap;

/**
 * 用于存储图文弹幕信息的类
 */
public class BMPString {
	public String[] strArr;
	public ArrayList<Bitmap> bmpList;

	/**
	 * BMPString构造函数
	 * 
	 * @param strArr
	 *            字符串数组,代表被图片分割的字符串
	 * @param bmpList
	 *            图片数组 , 第i张图片在弹幕中的位置是strArr[i]之后
	 */
	public BMPString(String[] strArr, ArrayList<Bitmap> bmpList) {
		this.strArr = strArr;
		this.bmpList = bmpList;
	}
}