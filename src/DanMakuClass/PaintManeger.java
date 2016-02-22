package DanMakuClass;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

/**
 * 画笔管理类，管理画笔颜色
 * Created by 晨晖 on 2015-07-31.
 */
public class PaintManeger {
	//弹幕画笔缓冲池 弃用
    //private static Map<String, Paint> paintPool;

    private static PaintManeger paintManeger;
    private Paint mPaint;
    private static Paint mBmpPaint ;
    private static Paint mMeasurePaint ;
    
    private PaintManeger() {
    	mBmpPaint = new Paint();
    	mPaint = new Paint();
    	mPaint.setAntiAlias(true);
        //paint.setTypeface(Typeface.DEFAULT_BOLD);
    	mPaint.setDither(true);
    	mPaint.setStrokeWidth(DanMakuViewSettings.DANMAKU_STROKE_WIDTH);
    	mPaint.setTextSize(DanMakuViewSettings.DANMAKU_TEXT_SIZE);
    	mPaint.setStyle(Style.FILL_AND_STROKE);
    	mPaint.setShadowLayer(DanMakuViewSettings.DANMAKU_STROKE_WIDTH, 0, 0, Color.BLACK);
    }

    public synchronized static PaintManeger getInstance() {
        if (paintManeger == null) {
          synchronized(PaintManeger.class){
			if(paintManeger == null){
				  paintManeger = new PaintManeger();
			}
          }
        }
        return paintManeger;
    }

    /**
     * 获取对应颜色值的画逼,修改画笔类的颜色
     *
     * @param color 颜色字符串，类似#000000
     * @return
     */
    public Paint getPaintByColor(String color) {
    	mPaint.setColor(Color.parseColor(color));
        return mPaint;
    }

    /**
     * 修改画笔阴影宽度
     */
    public void changeAllPaintStrokeWidth(){
    	if(DanMakuViewSettings.DANMAKU_STROKE_WIDTH < 0){
    		return ;
    	}
    	mPaint.setShadowLayer(DanMakuViewSettings.DANMAKU_STROKE_WIDTH, 0, 0, Color.BLACK);
    }
    /**
     * 用于弹幕精灵宽度测量
     * @param danMakuSprite
     * @return
     */
    public static float  measureDanMaKuLength(int textSize , String message){
    	if(message == null){
    		return 0;
    	}
    	if(mMeasurePaint == null){
    		mMeasurePaint = new Paint();
    	}
    	mMeasurePaint.setTextSize(textSize * DanMakuViewSettings.consentItemHeight);
        float length = mMeasurePaint.measureText(message);
        return length;
    }
    
    /**
     * 获取绘制BMP画笔
     * @return
     */
    public  Paint getBMPPaint(){
    	return mBmpPaint;
    }
}
