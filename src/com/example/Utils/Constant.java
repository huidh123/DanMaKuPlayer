package com.example.Utils;

import com.example.AndroidUtils.SDCardUtils;



/**
 * Created by 晨晖 on 2015-05-14.
 */
public class Constant {

	public static final String BILIBILI_APP_KEY = "03fc8eb101b091fb";
    public static final String GET_CHAT_FILE_SERVER_URL = "http://comment.bilibili.com/%s.xml";

    public static final String DANMU_FILE_SAVE_PATH = SDCardUtils.getSDCardPath()+"/GiliGili/";
    
    public static final String GET_VEDIO_INFO_BY_AVNUM = "http://api.bilibili.cn/view?type=json&appkey="+BILIBILI_APP_KEY+"&id=%s";
    
    public static final String GET_VEDIO_PATH = "http://www.bilibili.com/m/html5?aid=%s";
    /**
     * 将毫秒数转化为字符显示
     * @param millisecond
     * @return
     */
    public static String changeMSTimeToStr(long millisecond) {
        long seconds = millisecond / 1000;
        long minute = seconds / 60;
        long second = seconds % 60;
        String resStr = minute + ":" + second;
        return resStr;
    }

}
