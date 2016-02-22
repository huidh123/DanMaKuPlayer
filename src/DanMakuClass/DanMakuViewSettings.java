package DanMakuClass;

import DanMakuClass.DanMaKuViewConstants.FRESH_MODE;

public class DanMakuViewSettings {
	/**
	 * 各种参数系数
	 */
	 public static float SPEED_RATE = 1.0f;
	 public static float ALPHA_RATE = 1.0f;
	/**
	 * 弹幕描边宽度
	 */
	public static int DANMAKU_STROKE_WIDTH = 2;

	/**
	 * 弹幕描边颜色
	 */
	public static final String DANMAKU_STROKE_COLOR = "#80000000";
	/**
	 * 弹幕字号大小,计算单位为:consentItemHeight,
	 * <br>
	 * 每个字号代表字体大小为:(DANMAKU_TEXT_SIZE * consentItemHeight)
	 */
	public static int DANMAKU_TEXT_SIZE = 10;
	
	/**
	 * 同行弹幕之间相距距离,防止重叠
	 */
	public static int judgeOffCon = 100;

	/**
	 * 同行弹幕间距,单位:consentItemHeight
	 */
	public static int DANMAK_CHANNEL_DIFF = 3;
	
	/**
	 * 弹幕移动所需时间
	 */
	public static int DANMAKU_ACROSS_TIMEMS = 5000;
	
	/**
	 * 每个弹幕管道分割管道宽度 , 单位PX
	 */
	public static int consentItemHeight = 10;
	
	/**
	 * 弹幕管道分割数量 
	 */
    public static int maxColumes = 100;
    
    /**
     * 弹幕视差偏移量,单位像素,这个单位是当代表最大偏移量，具体偏移会结合参数计算
     */
    public static int parallax_delta = 20;
    
    /**
     * 视差最大偏移角度
     */
    public static int MAX_PARALLAX_ANGLE = 30;
    
 
    /**
     * 弹幕控件刷新帧数
     */
    public static FRESH_MODE DANMAKU_VIEW_FRESHMODE = FRESH_MODE.FPS60;
    
}
