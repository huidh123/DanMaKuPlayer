package DanMakuClass;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.example.AndroidUtils.L;
import com.example.AndroidUtils.ScreenUtils;
import com.example.Utils.Constants;
import com.example.javaBean.BMPString;
import com.example.javaBean.DanMaKu;
import com.example.sdltestactivity.Interface.DanMaKuEnigineTimeDriver;
import com.example.sdltestactivity.Interface.Observer;
import com.example.sdltestactivity.Interface.Publisher;

/**
 * Created by 晨晖 on 2015-04-11.
 */
public class DanMaKuView extends SurfaceView implements SurfaceHolder.Callback,
		Publisher , IDanmakuViewControl{

	//private boolean isFreshThreadRun = false;
	public boolean isFPSshow = true;
	//private boolean isPause = true;
	private int sreenHeight;
	private int screenWidth;
	/**
	 * 弹幕控件刷新间隔毫秒数
	 */
	private int viewFreshTimeMills = 17;
	
	public Handler handler;
	private Context context;
	private SurfaceHolder surfaceHolder = null;
	private SuspendThread freshTh = null;
	private SuspendThread danmakuPositionFreshThread = null;

	private DanMaKuManeger danMuManager;
	private DanMaKuEnigineTimeDriver danMaKuEnigineTimeDriver = null;

	private long allplayTime = 0;
	// private static int playCompleteDanmakuCount = 0;
	// 视差弹幕偏移量
	//private float devitionRate = 1f;
	
	//private ArrayList<DanMaKu> playDanMaKuList;
	private ArrayList<Observer> observers;

	public static final String tag = "DanMaKuVIew";
	private int MAX_FPS_DRAWTIMES_LIMIT = 50;

	// 帧数计算使用的数据结构，保存一定数量的没帧绘图时间
	private List<Long> drawUseMills;
	private long lastDrawTime = 0;
	private Rect screenRect;

	/**
	 * 保存弹幕播放器的状态信息
	 */
	private DanmakuViewDrawStatus danmakuViewDrawStatus;

	static class MyHandler extends Handler {

		private WeakReference<DanMaKuView> danMaKuViewWeakReference;

		public MyHandler(DanMaKuView danMaKuView) {
			danMaKuViewWeakReference = new WeakReference<DanMaKuView>(
					danMaKuView);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			DanMaKuView danMaKuView = danMaKuViewWeakReference.get();
			if (msg.what == 1) {
				danMaKuView.notifyAllObserver();
			}
		}
	}

	public DanMaKuView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initSurfaceView();
	}

	public DanMaKuView(Context context) {
		super(context);
		this.context = context;
		initSurfaceView();
	}

	/**
	 * 获取弹幕管理器
	 */
	public DanMaKuManeger getDanmakuManager(){
		return this.danMuManager;
	}
	
	/**
	 * 设置DanMaKUView播放的弹幕源,耗时操作，请务必在子线程中调用
	 *
	 * @param playDanMaKuList
	 */
	public void setPlayDanMaKuList(ArrayList<DanMaKu> playDanMaKuList) {
		danMuManager.setDanMaKuList(playDanMaKuList);
	}

	/**
	 * 关闭弹幕播放器，回收资源，终止线程
	 */
	public void stopDanMakuView() {
		//终止线程
		freshTh.stopThread();
		danmakuPositionFreshThread.stopThread();
		//回收资源
		BitmapManager.getInstance().clearBitmapPool();
	}

	public long getAllplayTime() {
		return allplayTime;
	}

	/**
	 * 设置弹幕引擎当前播放时间，在此方法中会定位到当前应当播放弹幕条数
	 *
	 * @param allplayTime
	 */
	public void setAllplayTime(long allplayTime) {
		this.allplayTime = allplayTime;
		// 定位到当前应当播放弹幕条数，注意：1500为位移量，将屏幕清空
		danMuManager.seekDanmakuToTimeMills(allplayTime + 1500);
	}

	/**
	 * 初始化View
	 */
	private void initSurfaceView() {
		danMuManager = new DanMaKuManeger();
		surfaceHolder = getHolder();
		this.setZOrderMediaOverlay(true);// 设置画布 背景透明
		this.setWillNotCacheDrawing(true);
		this.setDrawingCacheEnabled(false);
		this.setWillNotDraw(true);
		surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
		surfaceHolder.addCallback(this);
		handler = new MyHandler(this);

		observers = new ArrayList<Observer>();

		sreenHeight = ScreenUtils.getScreenHeight(context);
		screenWidth = ScreenUtils.getScreenWidth(context);
		screenRect = new Rect(0, 0, screenWidth, sreenHeight);
		drawUseMills = new ArrayList<Long>();
		this.setLayoutParams(new ViewGroup.LayoutParams(screenWidth,
				sreenHeight));
		danmakuViewDrawStatus = new DanmakuViewDrawStatus();
	}

	/**
	 * 清除屏幕画布
	 * @param canvas
	 */
	protected void clearCanvas(Canvas canvas){
		if(canvas == null){
			return;
		}
		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
	}
	/**
	 * 重新设置弹幕播放View的尺寸
	 * 
	 * @param height
	 * @param width
	 */
	public void resetDanmakuViewSize(int height, int width) {
		sreenHeight = height;
		screenWidth = width;
		screenRect = new Rect(0, 0, screenWidth, sreenHeight);
		LayoutParams layoutParams = this.getLayoutParams();
		layoutParams.height = height;
		layoutParams.width = width;
		this.setLayoutParams(layoutParams);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int viewHeight = getMeasuredHeight();
		int viewWitdh = getMeasuredWidth();
		L.log("弹幕播放器测量高度=" + viewHeight + "宽度=" + viewWitdh);
		setMeasuredDimension(viewWitdh, viewHeight);
		synchronized(danMuManager.isReady){
			if(!danMuManager.isReady){
				if(viewHeight == 0 || viewWitdh == 0){
					Log.e("tag","弹幕播放器测量高度=" + viewHeight + "宽度=" + viewWitdh);
					return;
				}
				danMuManager.initDanMuManagerConfig(getContext(),viewHeight, viewWitdh, DanMakuViewSettings.maxColumes);
			}
		}
	}

	/**
	 * 循环调用的用于绘制的方法
	 * @return
	 */
	private long doDraw() {
		Canvas canvas = null;
		long realDrawTime = 0;
		long startTime = System.currentTimeMillis();
		canvas = surfaceHolder.lockCanvas(screenRect);
		if (canvas != null) {
			this.clearCanvas(canvas);
			synchronized (danMuManager.freshingDanmakuListLock) {
				long syncTimeStart = System.currentTimeMillis();
				danMuManager.renderAllShowingDanmaku(canvas);
				long syncTimeEnd = System.currentTimeMillis();
				realDrawTime = syncTimeEnd - syncTimeStart;
			}
			if (isFPSshow) {
				drawFPS(canvas);
				lastDrawTime = System.currentTimeMillis();
			}
			surfaceHolder.unlockCanvasAndPost(canvas);
		}
		long endTime = System.currentTimeMillis();
//		Log.e("方法用时计时", String.format("绘制方法计时:%s , 同步用时%s",
//				(endTime - startTime), ((endTime - startTime) - realDrawTime)));
		return (endTime - startTime);
	}

	/**
	 * 绘制FPS到屏幕上
	 * @param canvas
	 */
	private void drawFPS(Canvas canvas) {
		drawUseMills.add(lastDrawTime);
		if (drawUseMills.size() > MAX_FPS_DRAWTIMES_LIMIT) {
			drawUseMills.remove(0);
		}
		if(drawUseMills.size() == 0){
			return;
		}
		double tempFPS = DanMaKuViewConstants.MILLS_ONE_SECOND
				/ (lastDrawTime - drawUseMills.get(0)) * drawUseMills.size();
		//Log.e("DanmakuView","FPS为："+tempFPS);
		canvas.drawText(String.format("当前FPS:%1$.2f", tempFPS), 10, 400,
				PaintManeger.getInstance().getPaintByColor("#FF0000"));
	}

	@Override
	public void addObserver(Observer observer) {
		observers.add(observer);
	}

	@Override
	public void removeObserver(Observer observer) {
		observers.remove(observer);
	}

	@Override
	public void notifyAllObserver() {
		for (int i = 0; i < observers.size(); i++) {
			observers.get(i).update();
		}
	}

	/**
	 * 设置弹幕时间驱动，用于View与时间驱动进行同步
	 * 
	 * @param danMaKuEnigineTimeDriver
	 */
	public void setDanMaKuEnigineTimeDriver(
			DanMaKuEnigineTimeDriver danMaKuEnigineTimeDriver) {
		this.danMaKuEnigineTimeDriver = danMaKuEnigineTimeDriver;
	}

	public void removeDanMaKuEnigineTimeDriver() {
		this.danMaKuEnigineTimeDriver = null;
	}

	/**
	 * 设置弹幕字体大小
	 * 
	 * @param fontsSize
	 */
	public void setDanmakuFontsSize(int fontsSize) {
		DanMakuViewSettings.DANMAKU_STROKE_WIDTH = fontsSize;
	}

	/**
	 * 获取弹幕字体大小
	 * 
	 * @return
	 */
	public int getDanmakuFontsSize() {
		return DanMakuViewSettings.DANMAKU_TEXT_SIZE;
	}

	public void setDanmakuAlpha(float alphaRate) {
		if (alphaRate <= 0) {
			alphaRate = 0;
		} else if (alphaRate >= 1) {
			alphaRate = 1;
		}
		DanMakuViewSettings.ALPHA_RATE = alphaRate;
	}

	public float getDanmakuAlpha() {
		return DanMakuViewSettings.ALPHA_RATE;
	}

	/**
	 * 清除当前显示的全部弹幕
	 */
	public void clearAllDanMaku() {
	}

	public void setFreshMode(DanMaKuViewConstants.FRESH_MODE mode){
		DanMakuViewSettings.DANMAKU_VIEW_FRESHMODE = mode;
		if(DanMakuViewSettings.DANMAKU_VIEW_FRESHMODE == DanMaKuViewConstants.FRESH_MODE.FPS30){
			viewFreshTimeMills = 33;
		}else if(DanMakuViewSettings.DANMAKU_VIEW_FRESHMODE == DanMaKuViewConstants.FRESH_MODE.FPS60){
			viewFreshTimeMills = 17;
		}
	}
	/**
	 * 设置弹幕描边宽度
	 * 
	 * @param strokeWitdh
	 */
	public void setDanmakuStrokeWitdh(int strokeWitdh) {
		DanMakuViewSettings.DANMAKU_STROKE_WIDTH = strokeWitdh;
		// 修改完弹幕描边参数之后，刷新所有画笔
		PaintManeger.getInstance().changeAllPaintStrokeWidth();
	}

	public float getDanmakuSpeed() {
		return DanMakuViewSettings.SPEED_RATE;
	}

	/**
	 * 获取弹幕描边宽度
	 * 
	 * @return
	 */
	public int getDanmakuStrokeWidth() {
		return DanMakuViewSettings.DANMAKU_STROKE_WIDTH;
	}

	public void setDanmakuSpeed(float speedRate) {
		L.log("速度系数:" + speedRate);
		DanMakuViewSettings.SPEED_RATE = speedRate;
	}

	/**
	 * 修改弹幕行数
	 * 
	 * @param channelNum
	 */
	public void setDanmakuChannelNum(int channelNum) {
		DanMakuViewSettings.maxColumes = channelNum;
		// 调用弹幕Manager修改管道数量
		danMuManager.changeDanmakuChannelsNum();
	}

	/**
	 * 获取弹幕播放行数
	 * 
	 * @param speedRate
	 */
	public int getDanmakuChannelNum() {
		return DanMakuViewSettings.maxColumes;
	}

	/**
	 * 播放一条新的弹幕
	 * @param danMaKu
	 * @param isShowNow
	 */
	public void addDanmaku(DanMaKu danMaKu , boolean isShowNow){
		this.danMuManager.addDanmaku(danMaKu, isShowNow,allplayTime);
	}
	/**
	 * 用于弹幕引擎与时间驱动同步方法
	 * 
	 * @return 0 相差时间在误差范围之内 ， 无需同步 >0 弹幕播放器时间快于时间驱动 <0 弹幕播放器事件慢于时间驱动
	 */
	private long synchronousWithTimeDriver() {
		long driveTime;
		if (danMaKuEnigineTimeDriver == null) {
			driveTime = allplayTime;
		} else {
			driveTime = danMaKuEnigineTimeDriver.getCurTimeInMills();
		}
		if ((allplayTime - driveTime) < 400) {
			return 0;
		}
		return (allplayTime - driveTime);
	}

	/**
	 * 弹幕播放器当前播放状态
	 * 
	 * @author 晨晖
	 *
	 */
	class DanmakuViewDrawStatus {
		public long totalDrawFrameTime;
		public long drawUseTime;
		public long caculateUseTime;
		public DanMaKuManeger.DanMakusStatus danMakuManagerStatus;
	}

	/**
	 * 弹幕位置刷新线程
	 * @author 晨晖
	 */
	class OnDanmakuPositionFreshThread extends SuspendThread{

		//上一次添加弹幕的时间
		long lastAddTime = 0;
		
		long frameTime1 = 0;
		long frameTime2 = 0;
		@Override
		public void runLogic() {
			try {
				
				//计算用时
				frameTime1 = System.currentTimeMillis();
				Log.e("tag","位置计算用时："+(frameTime1 - frameTime2));
				frameTime2 = frameTime1;
				
				long startTimeMills = System.currentTimeMillis();
				danMuManager.addWaitingDanmaku(lastAddTime,allplayTime);
				lastAddTime = allplayTime;
				danmakuViewDrawStatus.danMakuManagerStatus = danMuManager.freshAllShowingDanMu(allplayTime);
				long stopTimeMills = System.currentTimeMillis() - startTimeMills;
				L.log(String.format("弹幕当前刷新时间:%s , 弹幕刷新进程暂停毫秒数:%s", allplayTime,(viewFreshTimeMills - stopTimeMills)));
				long pauseTime = Math.max((viewFreshTimeMills - stopTimeMills),0);
				Thread.currentThread().sleep(pauseTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}		
		}
	}
	/**
	 * surface刷新线程
	 */
	class OnFreshCanvasRunable extends SuspendThread{

		long lastFrameTime =0 ;
		long curFrameTime = 0;
		//上一帧暂停时间
		long lastDrawTimeMills = 0;
		
		long frameTime1 = 0;
		long frameTime2 = 0;
		
		@Override
		public void runPre() {
			lastFrameTime = System.currentTimeMillis();
			curFrameTime = System.currentTimeMillis();
		}
		@Override
		public void runLogic() {
			try {
				synchronized (surfaceHolder) {
					handler.sendEmptyMessage(1);
					// 绘制弹幕
					lastDrawTimeMills = doDraw();
					
					//计算用时
					frameTime1 = System.currentTimeMillis();
					Log.e("tag","绘制用时："+(frameTime1 - frameTime2));
					frameTime2 = frameTime1;
					// 同步时间驱动
					long deltaTime = synchronousWithTimeDriver();
					if (deltaTime == 0) {
						allplayTime += lastDrawTimeMills;
					} else if (deltaTime > 0) {
						L.log("时间驱动与引擎事件差距过大");
						L.log("误差时间:" + deltaTime);
					} else {
						L.log("弹幕播放器时间小于视频帧时间:"+deltaTime);
						allplayTime += lastDrawTimeMills;
					}	
					Thread.sleep(Math.max(0, (viewFreshTimeMills - lastDrawTimeMills)));
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 启动弹幕播放器播放
	 */
	@Override
	public boolean start() {
		synchronized(danMuManager.isReady){
			if(!danMuManager.isReady){
				danMuManager.initDanMuManagerConfig(context, this.getMeasuredHeight(), this.getMeasuredWidth(), DanMakuViewSettings.maxColumes);
			}
		}
		freshTh = new OnFreshCanvasRunable();
		freshTh.setName("绘图刷新线程");
		freshTh.start();
		
		danmakuPositionFreshThread = new OnDanmakuPositionFreshThread();
		danmakuPositionFreshThread.setName("弹幕刷新线程");
		danmakuPositionFreshThread.start();
		return true;
	}

	/**
	 * 设置弹幕播放暂停
	 */
	@Override
	public void pause() {
		//需要设置两个线程是否都暂停
		Log.e("tag","弹幕播放器暂停播放");
		if(freshTh != null){
			freshTh.setPause(true);
		}
		if(danmakuPositionFreshThread != null){
			danmakuPositionFreshThread.setPause(true);
		}		
	}
	
	/**
	 * 设置弹幕播放继续
	 */
	@Override
	public void resume() {
		Log.e("tag","弹幕播放器继续播放");
		if(freshTh != null){
			freshTh.setPause(false);
		}
		if(danmakuPositionFreshThread != null){
			danmakuPositionFreshThread.setPause(false);
		}
	}

	@Override
	public void hide() {
		this.clearAllDanMaku();
		this.pause();
		this.setVisibility(VISIBLE);
	}

	@Override
	public void show() {
		this.clearAllDanMaku();
		this.resume();
		this.setVisibility(INVISIBLE);
	}

	/**
	 * 停止弹幕播放
	 */
	@Override
	public void stop() {
		Log.e("tag","停止弹幕播放");
		freshTh.stopThread();
		danmakuPositionFreshThread.stopThread();
		BitmapManager.getInstance().clearBitmapPool();
	}

	@Override
	public void clear() {
		danMuManager.clearShowingDanmaku();
	}
	
	/**
	 * 是否播放暂停
	 * @return
	 */
	public boolean isPause() {
		return freshTh.isPause();
	}
	
	/**
	 * view生命周期方法
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.e(tag, "surfaceCreated");
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.e(tag, "弹幕界面改变");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.e(tag, "弹幕界面被销毁");
	}
}
