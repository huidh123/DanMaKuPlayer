package DanMakuClass;

import DanmakuSP.DanMakuSprite;
import DanmakuSP.MutiColumeDanMakuSP;
import DanmakuSP.RtoLDanMakuSP;
import DanmakuSP.TopDanMakuSP;

import com.example.AndroidUtils.L;
import com.example.javaBean.BMPString;
import com.example.javaBean.DanMaKu;


/**
 * 创建弹幕精灵对象静态工厂
 * Created by 晨晖 on 2015-07-31.
 */
public class DanMaKuSPFactory {
	
    /**
     * 创建弹幕精灵对象
     *
     * @param danmakuType
     * @param useDanmeChannel
     * @param curX
     * @param curY
     * @param color
     * @param showTimeMS
     * @return
     */
    public static DanMakuSprite createDanMaKuSP(int danmakuType,int danmakuFontsSize, int useDanmeChannel,int zOrder, int curX, int curY, String color, long showTimeMS, Object message) {
    	int tempFontsSize = (int)(danmakuFontsSize / DanMakuViewSettings.consentItemHeight);
        if (danmakuType == DanMaKuViewConstants.DANMU_TYPE_RIGHT_TOLEFT) {
        	DanMakuSprite danMakuSprite = new RtoLDanMakuSP((String)message , showTimeMS , color , tempFontsSize);
            return danMakuSprite;
        }else if (danmakuType == DanMaKuViewConstants.DANMU_TYPE_TOP) {
        	DanMakuSprite danMakuSprite = new TopDanMakuSP((String)message ,showTimeMS ,color , tempFontsSize);
            return danMakuSprite;
        }else if(danmakuType == DanMaKuViewConstants.DANMU_TYPE_MUTLI){
        	DanMakuSprite danMakuSprite = new MutiColumeDanMakuSP((String)message , showTimeMS , color , tempFontsSize);
        	return danMakuSprite;        	
        }else if(danmakuType == DanMaKuViewConstants.DANMU_TYPE_BMP){
        	//DanMakuSprite danmakuSP = new IconDanmakuSP((BMPString) message, showTimeMS,color, tempFontsSize);
        	return null;
        }else {
        	RtoLDanMakuSP danMakuSprite = new RtoLDanMakuSP((String)message , showTimeMS  , color  , tempFontsSize);
            return danMakuSprite;
        }
    }
    

    /**
     * 创建弹幕精灵对象
     */
    public static DanMakuSprite createDanMaKuSP(DanMaKu danMaKu){
    	return createDanMaKuSP(danMaKu.getDanmuType() , danMaKu.getDanmakuTextSize() , 0 , 0 , 0 , 0 ,danMaKu.getDanmuColor() , danMaKu.getShowTime() , danMaKu.getDanmakuContent());
    }

}
