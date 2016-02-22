package DanMakuClass;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import com.example.AndroidUtils.L;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;


/**
 * 弹幕图片的Bitmap缓冲池,用于保存弹幕的绘制图片 Created by 晨晖 on 2015-09-19.
 */
public class BitmapManager {
	private static BitmapManager bitmapManager;
	private HashMap<String, Bitmap> bitmapPoolMap;

	private PaintManeger paintManeger;
	
	/**
	 * 当前BMP缓冲池占用内存大小
	 */
	private long curMemory = 0;
	
	private BitmapManager() {
		bitmapPoolMap = new HashMap<String, Bitmap>();
		this.paintManeger = PaintManeger.getInstance();
	}

	public static BitmapManager getInstance() {
		if (bitmapManager == null) {
			synchronized(BitmapManager.class){
				if(bitmapManager == null){
					bitmapManager = new BitmapManager();
				}
			}
		}
		return bitmapManager;
	}

	
	/**
	 * 添加一个弹幕BMP进入缓冲池
	 * @param danmakuBmp
	 * @return
	 */
	public String addDanmakuBitmap(Bitmap danmakuBMP){
		String key = UUID.randomUUID().toString();
		bitmapPoolMap.put(key,danmakuBMP);
		curMemory += danmakuBMP.getByteCount();
		return key;
	}

	public Bitmap getBitmapById(String id) {
		return bitmapPoolMap.get(id);
	}


	/**
	 * 获取BMP池缓存数量
	 * @return
	 */
	public int getBmpCacheSize() {
		return bitmapPoolMap.size();
	}
	

	/**
	 * 清除一个缓冲区贴图
	 * @param bmpId
	 */
	public void removeBitmap(String bmpId) {
		Bitmap needMoveBmp = bitmapPoolMap.get(bmpId);
		if(needMoveBmp != null && !needMoveBmp.isRecycled()){
			curMemory -= needMoveBmp.getByteCount();
			needMoveBmp.recycle();
		}
		bitmapPoolMap.remove(bmpId);
	}
	
	/**
	 * 清除全部缓冲区弹幕贴图
	 */
	public void clearBitmapPool(){
		for(Entry<String , Bitmap> entry : this.bitmapPoolMap.entrySet()){
			if(!entry.getValue().isRecycled()){
				entry.getValue().recycle();
			}
		}
		this.bitmapPoolMap = null;
		System.gc();
		curMemory = 0;
		this.bitmapPoolMap  = new HashMap<String, Bitmap>();
	}
	
	/**
	 * 创建一个弹幕的缓存Bitmap，以后只需要绘制这个Bitmap即可
	 *
	 * @param height
	 * @param width
	 * @param message
	 * @return 弹幕Bitmap的标识符(UUID计算)
	 */
	public static Bitmap createDanmakuBmp(int width, int height, int fontsSize,int zOrder ,String message, String color) {
        if(height <= 0 || width <= 0){
        	L.log("弹幕参数错误，弹幕："+message);
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        //Log.e("BitmapManager", String.format("createBitmap height :%d , width = %d", height, width));
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.argb(0, 100, 0, 0));
        //由于直接绘制text是基于基准线的，需要有6px的偏移
        Paint tempPaint = PaintManeger.getInstance().getPaintByColor(color);
        tempPaint.setTextSize(fontsSize);
        tempPaint.setAlpha((int) (255* DanMakuViewSettings.ALPHA_RATE));
        canvas.drawText(message, 0, height - 6, tempPaint);
        return bitmap;
    }
	
	/**
	 * 获取当前缓冲区占用内存大小
	 * @return
	 */
	public long getCurMemory(){
		return this.curMemory;
	}
}
