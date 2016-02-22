package DanmakuSP;

import DanMakuClass.ChannelManeger;
import DanMakuClass.DanMaKuViewConstants;
import DanMakuClass.DanMakuViewSettings;
import DanMakuClass.PaintManeger;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

public class MutiColumeDanMakuSP extends DanMakuSprite{

	String [] messageStrArr;
	int maxLength;
	int maxLengthIndex;
	float moveSpeed;
	
	private PaintManeger paintManeger;
	
	public MutiColumeDanMakuSP(String message , long showTimeMS  , String danmuColor  , int danmuFontsSize) {
		super(message, showTimeMS, danmuColor, danmuFontsSize, DanMaKuViewConstants.DANMU_TYPE_RIGHT_TOLEFT);
		paintManeger = PaintManeger.getInstance();
		messageStrArr = message.split("\n");
		for(int i = 0 ; i < messageStrArr.length ; i++){
			if(maxLength < messageStrArr[i].length()){
				maxLengthIndex = i;
				maxLength = messageStrArr[i].length();
			}
		}
		this.danmuLength = PaintManeger.measureDanMaKuLength(danmuFontsSize, messageStrArr[maxLengthIndex]);
		this.calculateMoveSpeed();
		this.curX = DanMaKuViewConstants.viewWidth;
		this.useChannelNum = danmuFontsSize * messageStrArr.length + 1;
	}
	
	@Override
	public void freshSprite(long curMills) {
		this.curX -= moveSpeed * 10;
	}
	
	public void calculateMoveSpeed() {
		this.moveSpeed = (this.danmuLength + DanMakuViewSettings.judgeOffCon
				* 2 + DanMaKuViewConstants.viewWidth)
				/ (DanMakuViewSettings.DANMAKU_ACROSS_TIMEMS / DanMakuViewSettings.SPEED_RATE);
	}
	
	@Override
	public void layoutSprite(ChannelManeger channelManager) {
		int freeChannelIndex = channelManager.getFreeColumeIndex(this.danmakuType,this.useChannelNum);
		if(freeChannelIndex != -1){
			channelManager.setIsChannelUsed(this.danmakuType,this.useDanmeChannel, this.useChannelNum, true);
			this.setLayoutReady(true);
		}else{
			this.setLayoutReady(false);
		}
	}

	@Override
	protected Bitmap createDanmakuBMP() {
		if(this.danmuLength <= 0){
			Log.e("tag", String.format("弹幕长度错误 , 弹幕内容:%s", this.message));
			return null;
		}
		Bitmap tempBMP = Bitmap.createBitmap((int) this.danmuLength,useChannelNum * DanMakuViewSettings.consentItemHeight , Config.ARGB_8888);
		Canvas canvas = new Canvas(tempBMP);
		Paint tempPaint = paintManeger.getPaintByColor(this.danmuColor);
		tempPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		for(int i = 0; i < messageStrArr.length ; i++){
			tempPaint.setTextSize(this.danmuFontsSize * DanMakuViewSettings.consentItemHeight);
			canvas.drawText(messageStrArr[i], 0, (i+1) * (danmuFontsSize * DanMakuViewSettings.consentItemHeight), tempPaint);
		}
		canvas.drawColor(Color.parseColor("#50FF0000"));
		return tempBMP;
	}
}
