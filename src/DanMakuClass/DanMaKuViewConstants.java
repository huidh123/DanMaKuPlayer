package DanMakuClass;

import java.util.Comparator;
import java.util.Random;

import DanmakuSP.DanMakuSprite;

import com.example.javaBean.DanMaKu;

/**
 * Created by 晨晖 on 2015-07-31.
 */
public class DanMaKuViewConstants {

	/**
	 * 控件高度
	 */
	public static int viewHeight;
	/**
	 * 控件宽度
	 */
	public static int viewWidth;

	/**
	 * 弹幕相对屏幕状态
	 */
	public enum DanmuState {
		en_outOfScreen, en_totallyInScreen, en_onScreenBounding
	}
	
	/**
	 * 弹幕中出现的BMP固定大小
	 */
	public static int DANMAKU_BMP_SIZE = 108;

	/**
	 * 弹幕控件刷新模式
	 * @author 晨晖
	 *
	 */
	public enum FRESH_MODE{
		FPS30 , FPS60
	};
	    
	/**
	 * 弹幕类型标示符
	 */
	public static final int DANMU_TYPE_RIGHT_TOLEFT = 1;
	public static final int DANMU_TYPE_TOP = 4;
	public static final int DANMU_TYPE_MUTLI = 1000;
	public static final int DANMU_TYPE_BMP = 1001;

    public static double MILLS_ONE_SECOND = 1000;
	/**
	 * Z轴排序
	 */
	public static Comparator<DanMakuSprite> zOrderComparetor = new Comparator<DanMakuSprite>() {
		@Override
		public int compare(DanMakuSprite lhs, DanMakuSprite rhs) {
			// TODO Auto-generated method stub
			if (lhs.ZOrderIndex > rhs.ZOrderIndex) {
				return 1;
			} else if (lhs.ZOrderIndex == rhs.ZOrderIndex) {
				return 0;
			} else {
				return -1;
			}
		}
	};
	
	/**
	 * 用于弹幕时间排序的类
	 */
	public static class SortByTimeMills implements Comparator<DanMaKu> {

		@Override
		public int compare(DanMaKu lhs, DanMaKu rhs) {
			if (lhs.getShowTime() < rhs.getShowTime()) {
				return -1;
			} else if (lhs.getShowTime() > rhs.getShowTime()) {
				return 1;
			}
			return 0;
		}
	}
	
	public static Random randomZIndex = new Random();
}