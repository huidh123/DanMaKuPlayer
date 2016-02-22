package com.example.javaBean;


/**
 * Created by 晨晖 on 2015-04-25.
 */
public class DanMaKu {
    private Object danmakuContent;
    private int showTime;
    private int danmuType;
    private String danmuColor;
    private int danmakuTextSize;
    
    public DanMaKu() {
    }
    
    public Object getDanmakuContent() {
		return danmakuContent;
	}

	public void setDanmakuContent(Object danmakuContent) {
		this.danmakuContent = danmakuContent;
	}

	public int getShowTime() {
		return showTime;
	}

	public void setShowTime(int showTime) {
		this.showTime = showTime;
	}

	public int getDanmuType() {
		return danmuType;
	}

	public void setDanmuType(int danmuType) {
		this.danmuType = danmuType;
	}

	public String getDanmuColor() {
		return danmuColor;
	}

	public void setDanmuColor(String danmuColor) {
		this.danmuColor = danmuColor;
	}

	public int getDanmakuTextSize() {
		return danmakuTextSize;
	}

	public void setDanmakuTextSize(int danmakuTextSize) {
		this.danmakuTextSize = danmakuTextSize;
	}

	public DanMaKu(DanMaKu tempDanmaku){
    	 this.danmakuContent = tempDanmaku.danmakuContent;
         this.showTime = tempDanmaku.showTime;
         this.danmuType = tempDanmaku.danmuType;
         this.danmuColor = tempDanmaku.danmuColor;
         this.danmakuTextSize = tempDanmaku.danmakuTextSize;
    }
}
