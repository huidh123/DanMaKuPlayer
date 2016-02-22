package DanmakuSP;

import android.graphics.Bitmap;
import DanMakuClass.BitmapManager;
import DanMakuClass.ChannelManeger;
import DanMakuClass.DanMaKuViewConstants;
import DanMakuClass.DanMakuViewSettings;
import DanMakuClass.PaintManeger;
import DanMakuClass.DanMaKuViewConstants.DanmuState;

/**
 * Created by 晨晖 on 2015-08-29.
 */
public class TopDanMakuSP extends DanMakuSprite {

	public TopDanMakuSP(String message, long showTimeMS,String danmuColor, int danmuFontsSize) {
		super(message, showTimeMS, danmuColor, danmuFontsSize, DanMaKuViewConstants.DANMU_TYPE_TOP);
		this.danmuLength = PaintManeger.measureDanMaKuLength(this.danmuFontsSize, this.message);
		this.curX = (DanMaKuViewConstants.viewWidth - (int) this.danmuLength) / 2;
		this.useChannelNum = danmuFontsSize;
	}

	@Override
	public void freshSprite(long curMills) {
		if ((curMills - this.showTimeMS) > DanMakuViewSettings.DANMAKU_ACROSS_TIMEMS) {
			this.danmuCurState = DanMaKuViewConstants.DanmuState.en_outOfScreen;
		} else {
			this.danmuCurState = DanMaKuViewConstants.DanmuState.en_onScreenBounding;
		}
	}

	@Override
	public DanMaKuViewConstants.DanmuState getDanMaKuStatus() {
		return this.danmuCurState;
	}
	
	@Override
	public void layoutSprite(ChannelManeger channelManager) {
		int freeChannelIndex = channelManager.getFreeColumeIndex(this.danmakuType,this.useChannelNum);
		if(freeChannelIndex != -1){
			channelManager.setIsChannelUsed(this.danmakuType,this.useDanmeChannel, this.useChannelNum, true);
			this.setLayoutReady(true);
			this.useDanmeChannel = freeChannelIndex;
			this.curX = (DanMaKuViewConstants.viewWidth - (int) this.danmuLength) / 2;
			this.curY = useDanmeChannel * DanMakuViewSettings.consentItemHeight;
		}else{
			this.setLayoutReady(false);
		}
	}

	@Override
	protected Bitmap createDanmakuBMP() {
		// TODO Auto-generated method stub
		return BitmapManager.createDanmakuBmp((int) danmuLength,
				danmuFontsSize * DanMakuViewSettings.consentItemHeight,
				danmuFontsSize * DanMakuViewSettings.consentItemHeight,
				this.ZOrderIndex, message, danmuColor);
	}
}
