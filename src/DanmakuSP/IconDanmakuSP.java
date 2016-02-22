//package DanmakuSP;
//
//import DanMakuClass.ChannelManeger;
//import DanMakuClass.DanMaKuViewConstants;
//import DanMakuClass.DanMakuViewSettings;
//import android.graphics.Bitmap;
//import android.graphics.Bitmap.Config;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//
//import com.example.AndroidUtils.L;
//import com.example.javaBean.BMPString;
//
//public class IconDanmakuSP extends DanMakuSprite {
//
//	private BMPString bmpText;
//	private float moveSpeed;
//
//	public IconDanmakuSP(BMPString bmpStr, long showMills,
//			String danmuColor, int danmuFontsSize) {
//		super();
//		this.danmakuType = DanMaKuViewConstants.DANMU_TYPE_RIGHT_TOLEFT;
//		this.bmpText = bmpStr;
//		this.showTimeMS = showMills;
//		this.ZOrderIndex = zOrderIndex;
//		this.danmuColor = danmuColor;
//		this.danmuFontsSize = danmuFontsSize;
//		this.useDanmeChannel = useDanmuChannel;
//		this.useChannelNum = (DanMaKuViewConstants.DANMAKU_BMP_SIZE / DanMakuViewSettings.consentItemHeight) + 1;
//		this.danmuLength = colculateDanmakuLenght(bmpStr, danmuFontsSize);
//		this.calculateMoveSpeed();
//		this.curX = DanMaKuViewConstants.viewWidth;
//	}
//
//	@Override
//	public void freshSprite(long curMills) {
//		this.curX -= moveSpeed * 17;
//	}
//
//	/**
//	 * 计算图文弹幕的长度
//	 * 
//	 * @param bmpStr
//	 * @param danmakuTextSize
//	 * @return
//	 */
//	private int colculateDanmakuLenght(BMPString bmpStr, int danmakuTextSize) {
//		int resLength = 0;
//		for (int i = 0; i < bmpStr.strArr.length; i++) {
//			resLength += this.paintManeger.measureDanMaKuLength(danmakuTextSize, message);
//		}
//		resLength += (DanMaKuViewConstants.DANMAKU_BMP_SIZE)
//				* bmpStr.bmpList.size();
//		return resLength;
//	}
//
//	/**
//	 * 计算弹幕位移距离
//	 */
//	private void calculateMoveSpeed() {
//		this.moveSpeed = (this.danmuLength + DanMakuViewSettings.judgeOffCon
//				* 2 + DanMaKuViewConstants.viewWidth)
//				/ (DanMakuViewSettings.DANMAKU_ACROSS_TIMEMS / DanMakuViewSettings.SPEED_RATE);
//	}
//
//	@Override
//	public void layoutSprite(ChannelManeger channelManager) {
//		int freeChannelIndex = channelManager.getFreeColumeIndex(this.danmakuType,this.useChannelNum);
//		if(freeChannelIndex != -1){
//			channelManager.setIsChannelUsed(this.danmakuType,this.useDanmeChannel, this.useChannelNum, true);
//			this.setLayoutReady(true);
//		}else{
//			this.setLayoutReady(false);
//		}
//	}
//
//	@Override
//	protected Bitmap createDanmakuBMP() {
//		Bitmap resBMP = Bitmap.createBitmap((int) this.danmuLength, DanMaKuViewConstants.DANMAKU_BMP_SIZE, Config.ARGB_8888);
//		Canvas canvas = new Canvas(resBMP);
//		int curLength = 0;
//		Paint tempPaint = this.paintManeger.getPaintByColor(this.danmuColor);
//		tempPaint.setTextSize(this.danmuFontsSize
//				* DanMakuViewSettings.consentItemHeight);
//		
//		L.log("生成图文弹幕:"+this.bmpText.strArr[0] + "弹幕大小:"+this.danmuFontsSize);
//		for (int i = 0; i < this.bmpText.strArr.length; i++) {
//			canvas.drawText(this.bmpText.strArr[i], curLength , this.danmuFontsSize * DanMakuViewSettings.consentItemHeight , tempPaint);
//			curLength += tempPaint.measureText(this.bmpText.strArr[i]);
//			if(i < this.bmpText.bmpList.size()){
//				canvas.drawBitmap(this.bmpText.bmpList.get(i), curLength , 0 , tempPaint);
//				curLength += DanMaKuViewConstants.DANMAKU_BMP_SIZE;
//			}
//		}
//		return resBMP;
//	}
//}
