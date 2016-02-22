package DanmakuSP;

import DanMakuClass.BitmapManager;
import DanMakuClass.ChannelManeger;
import DanMakuClass.DanMaKuViewConstants;
import DanMakuClass.DanMakuViewSettings;
import DanMakuClass.PaintManeger;
import DanMakuClass.DanMaKuViewConstants.DanmuState;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;


/**
 * 弹幕精灵父类 Created by 晨晖 on 2015-07-31.
 */
public abstract class DanMakuSprite {

	public String message;
	public long showTimeMS;//弹幕发送时间
	public int danmakuType;//弹幕类型
	public String danmuColor;
	public int danmuFontsSize;
	
	public float curX;
	public float curY;
	public int ZOrderIndex;//弹幕Z轴序号，大的位于上方
	public int useDanmeChannel;//使用的起始管道编号
	public int useChannelNum;//使用的管道数量
	public DanMaKuViewConstants.DanmuState danmuCurState;
	public float danmuLength;
	public String bmpId;
	public Bitmap mDanmakuBMP;
	
	/**
	 * 是否弹幕布局完成
	 */
	private boolean isLayoutReady = false;

	protected PaintManeger paintManeger;
	protected BitmapManager bitmapManager;

	public DanMakuSprite() {
		bitmapManager = BitmapManager.getInstance();
		paintManeger = PaintManeger.getInstance();
		danmuCurState = DanMaKuViewConstants.DanmuState.en_totallyInScreen;
	}

	public DanMakuSprite(String message, long showTimeMS, String danmuColor, int danmuFontsSize,
			int danmakuType) {
		this();
		if(danmuFontsSize == 0){
			Log.e("tag","弹幕宽度为0:"+danmuFontsSize);
		}
		this.message = message; 
		this.showTimeMS = showTimeMS;
		this.danmuColor = danmuColor;
		this.danmuFontsSize = danmuFontsSize;
		this.danmakuType = danmakuType;
	}

	/**
	 * 绘制弹幕
	 * 
	 * @param canvas
	 */
	public void draw(Canvas canvas) {
		drawWithDeviation(canvas, 0, 0);
	}

	/**
	 * 
	 * @param canvas
	 * @param devX
	 * @param devY
	 */
	public void drawWithDeviation(Canvas canvas, int devX, int devY) {
		Bitmap tempBmp = this.mDanmakuBMP;
		if (tempBmp != null && !tempBmp.isRecycled()) {
			tempBmp.prepareToDraw();
			canvas.drawBitmap(tempBmp, curX + devX,
					curY + devY, paintManeger.getBMPPaint());
		}else{
			Log.e("tag","绘制失败");
		}
	}

	/**
	 * 抽象方法，用于精灵自身的刷新
	 */
	public abstract void freshSprite(long curMills);

	/**
	 * 对弹幕进行布局，设置弹幕显示位置和占用的管道，注意：请在布局完成后调用{@link setLayoutReady}设置布局完成，否则将被不显示弹幕
	 * @param channelManager
	 */
	public abstract void layoutSprite(ChannelManeger channelManager);
	
	/**
	 * 初始化缓存BMP
	 * @return
	 */
	protected abstract Bitmap createDanmakuBMP();
	/**
	 * 初始化BMP缓存,会调用子类中的{@link Bitmap createDanmakuBMP()}方法获取用于缓存的BMP
	 */
	public final void initBMPCache(){
		this.mDanmakuBMP = this.createDanmakuBMP();
		this.bmpId = bitmapManager.addDanmakuBitmap(this.mDanmakuBMP);
	}
	
	public void setLayoutReady(boolean isLayout){
		this.isLayoutReady = isLayout;
	}
	public boolean isLayoutReady(){
		return this.isLayoutReady;
	}
	
	/**
	 * 返回是否初始化了图片缓存
	 * @return
	 */
	public boolean isInitCache(){
		return bmpId != null;
	}
	
	/**
	 * 判断控件当前相对屏幕位置
	 *
	 * @return 精灵状态枚举
	 */
	public DanMaKuViewConstants.DanmuState getDanMaKuStatus() {
		if (this.curX > DanMaKuViewConstants.viewWidth
				+ DanMakuViewSettings.judgeOffCon
				|| this.curX < -(this.danmuLength + DanMakuViewSettings.judgeOffCon)) {
			danmuCurState = DanMaKuViewConstants.DanmuState.en_outOfScreen;
			return DanMaKuViewConstants.DanmuState.en_outOfScreen;
		} else if (this.curX <= DanMaKuViewConstants.viewWidth
				+ DanMakuViewSettings.judgeOffCon
				&& this.curX > (DanMaKuViewConstants.viewWidth
						- this.danmuLength - DanMakuViewSettings.judgeOffCon)) {
			danmuCurState = DanMaKuViewConstants.DanmuState.en_onScreenBounding;
			return DanMaKuViewConstants.DanmuState.en_onScreenBounding;
		}
		danmuCurState = DanMaKuViewConstants.DanmuState.en_totallyInScreen;
		return DanMaKuViewConstants.DanmuState.en_totallyInScreen;
	}
	
	/**
	 * 回收弹幕资源
	 */
	public void recycleDanMakuSp(){
		bitmapManager.removeBitmap(this.bmpId);
		this.bmpId=null;
	}
}