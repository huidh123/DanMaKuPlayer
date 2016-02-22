package DanmakuSP;

import DanMakuClass.BitmapManager;
import DanMakuClass.ChannelManeger;
import DanMakuClass.DanMaKuViewConstants;
import DanMakuClass.DanMakuViewSettings;
import DanMakuClass.PaintManeger;
import android.graphics.Bitmap;
import android.util.Log;


/**
 * Created by 晨晖 on 2015-07-31.
 */
public class RtoLDanMakuSP extends DanMakuSprite {
	public float moveSpeed;

	public RtoLDanMakuSP(String message, long showTimeMS, String danmuColor,int danmuFontsSize) {
		super(message, showTimeMS, danmuColor, danmuFontsSize, DanMaKuViewConstants.DANMU_TYPE_RIGHT_TOLEFT);
		this.danmuLength = PaintManeger.measureDanMaKuLength(
				this.danmuFontsSize, this.message);
		this.calculateMoveSpeed();
		//弹幕宽度增加一定宽度,可以隔开两行弹幕宽度
		this.useChannelNum = danmuFontsSize + 1;
	}

	/**
	 * 计算移动速度
	 */
	public void calculateMoveSpeed() {
		this.moveSpeed = (this.danmuLength + DanMakuViewSettings.judgeOffCon
				* 2 + DanMaKuViewConstants.viewWidth)
				/ (DanMakuViewSettings.DANMAKU_ACROSS_TIMEMS / DanMakuViewSettings.SPEED_RATE);
	}

	/**
	 * 弹幕位置刷新的方法 , 会在每个帧绘制之前调用
	 * 在此之中可以进行弹幕位置计算 . 由于暂时无法保证帧数稳定, 不推荐使用curmills计算出弹幕准确位置  , 会导致弹幕看起来卡顿 . 推荐以下写法 , 但是会导致弹幕位置不准确
	 * @param curMills 当前帧绘制时间 
	 */
	@Override
	public void freshSprite(long curMills) {
		curX = ((DanMaKuViewConstants.viewWidth + this.danmuLength) / DanMakuViewSettings.DANMAKU_ACROSS_TIMEMS) * ( this.showTimeMS - curMills) + DanMaKuViewConstants.viewWidth;
	}

	@Override
	public void layoutSprite(ChannelManeger channelManager) {
		//Log.e("tag",String.format("类型%s,占用宽度：%s",this.danmakuType,this.useChannelNum));
		int freeChannelIndex = channelManager.getFreeColumeIndex(this.danmakuType,this.useChannelNum);
		if(freeChannelIndex != -1){
			channelManager.setIsChannelUsed(this.danmakuType,freeChannelIndex, this.useChannelNum, true);
			this.useDanmeChannel = freeChannelIndex;
			this.setLayoutReady(true);
			this.curX = DanMaKuViewConstants.viewWidth;
			this.curY = useDanmeChannel * DanMakuViewSettings.consentItemHeight;
		}else{
			Log.e("tag","弹幕未找到可用管道");
			this.setLayoutReady(false);
		}
	}

	@Override
	protected Bitmap createDanmakuBMP() {
		Bitmap danmuCache = BitmapManager.createDanmakuBmp((int) danmuLength,
				useChannelNum * DanMakuViewSettings.consentItemHeight,
				danmuFontsSize * DanMakuViewSettings.consentItemHeight,
				this.ZOrderIndex, message, danmuColor);
		return danmuCache;
	}
}
