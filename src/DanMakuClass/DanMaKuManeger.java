package DanMakuClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import useless.GravitySensorManager;
import DanMakuClass.Fliter.FliterContext;
import DanMakuClass.Fliter.FliterManager;
import DanMakuClass.Fliter.IDanmakuFliter;
import DanmakuSP.DanMakuSprite;
import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;

import com.example.AndroidUtils.L;
import com.example.javaBean.BMPString;
import com.example.javaBean.DanMaKu;

/**
 * Created by 晨晖 on 2015-07-31.
 */
public class DanMaKuManeger {
	public static final String tag = "DanMuManager";

	protected DanmakuRetainer mDanmakuRetainer;
	
	/**
	 * 正在显示的弹幕列表
	 */
	protected CopyOnWriteArrayList<DanMakuSprite> showingDanmuSpList = new CopyOnWriteArrayList<DanMakuSprite>();
	/**
	 * 弹幕列表锁
	 */
	protected Object freshingDanmakuListLock = new Object();
	protected List<DanMakuSprite> waitingDanMuList = new CopyOnWriteArrayList<DanMakuSprite>();
	protected HashSet<DanMakuSprite> needToRemoveSpList = new HashSet<DanMakuSprite>();
	protected HashSet<DanMakuSprite> needToAddSpList = new HashSet<DanMakuSprite>();

	protected int maxColumes = 0;
	private ChannelManeger channelManeger;
	private BitmapManager bitmapManager;
	
	/**
	 * 是否已经准备就绪
	 */
	protected Boolean isReady = false;
	
	/**
	 * 弹幕过滤器列表
	 */
	private List<IDanmakuFliter> mDanmakuFliterList;
	private FliterContext mFliterContext;
	private FliterManager mFliterManager;

	public Context context;
	

	public DanMaKuManeger() {
		mFliterContext = new FliterContext(this);
		bitmapManager = BitmapManager.getInstance();
		mFliterManager = new FliterManager(mFliterContext);
		mDanmakuRetainer = new DanmakuRetainer();
	}

	public void initDanMuManagerConfig(Context context , int viewHeight,
			int viewWidth, int maxColumes) {
		Log.e("tag","初始化DanmakuManager");
		DanMaKuViewConstants.viewHeight = viewHeight;
		DanMaKuViewConstants.viewWidth = viewWidth;
		this.maxColumes = maxColumes;
		this.context = context;
		Log.e("tag","初始化channelmanager:"+Thread.currentThread().getName());
		if(channelManeger == null){
			channelManeger = new ChannelManeger(maxColumes);
		}
		this.isReady = true;
	}
	
	/**
	 * 设置弹幕管理器管理弹幕数据源
	 * @param playDanMaKuList
	 */
	protected void setDanMaKuList(ArrayList<DanMaKu> playDanMaKuList){
		mDanmakuRetainer.setPlayDanmmuList(playDanMaKuList);
	}

	/**
	 * 添加弹幕过滤器
	 * @param danmakuFliter
	 */
	public void addDanmakuFliter(IDanmakuFliter danmakuFliter){
		danmakuFliter.setFliterContext(mFliterContext);
		mFliterManager.addFliter(danmakuFliter);
	}
	
	/**
	 * 绘制全部弹幕
	 * @param canvas
	 */
	protected void renderAllShowingDanmaku(Canvas canvas){
		if(showingDanmuSpList == null){
			return;
		}
		Iterator<DanMakuSprite> iterator = showingDanmuSpList.iterator();
		while(iterator.hasNext()){
			iterator.next().draw(canvas);
		}
	}
	/**
	 * 更新弹幕位置方法，在每一帧中调用
	 */
	protected DanMakusStatus freshAllShowingDanMu(long curMills) {
		
		// 初始化本次弹幕逻辑计算的方法调用状态参数
		DanMakusStatus danMakusStatus = new DanMakusStatus();
		long methodStartTime = System.currentTimeMillis();

		channelManeger.clearAllChannelUser();
		int showingDanmuCount = showingDanmuSpList.size();
		synchronized(freshingDanmakuListLock){
			for (int i = 0; i < showingDanmuCount; i++) {
				DanMakuSprite tempDanMakuSprite = showingDanmuSpList.get(i);
				//Log.e("tag","占用宽度："+tempDanMakuSprite.useDanmeChannel+":"+tempDanMakuSprite.useChannelNum);
				// 获取弹幕与屏幕位置关系
				DanMaKuViewConstants.DanmuState curDanmuState = tempDanMakuSprite
						.getDanMaKuStatus();
				// 移除离开屏幕的弹幕
				if (curDanmuState == DanMaKuViewConstants.DanmuState.en_outOfScreen) {
					needToRemoveSpList.add(tempDanMakuSprite);
				}
				// 设置占用弹幕管道的弹幕
				else if (curDanmuState == DanMaKuViewConstants.DanmuState.en_onScreenBounding) {
					channelManeger.setIsChannelUsed(tempDanMakuSprite.danmakuType,
							tempDanMakuSprite.useDanmeChannel,
							tempDanMakuSprite.useChannelNum, true);
				}
			}
	
			// 遍历滚动等待弹幕列表，将其添加到滚动空闲管道
			// 测试每个循环时间
			long startTime = System.currentTimeMillis();
			for(int index = 0 ; index < waitingDanMuList.size() ; index++){
				DanMakuSprite danmuSP = waitingDanMuList.get(index);
				danmuSP.layoutSprite(channelManeger);
				if(!danmuSP.isLayoutReady()){
					Log.e("tag","弹幕未找到管道");
					continue;
				}
				if(!danmuSP.isInitCache()){
					Log.e("tag","弹幕未准备初始化");
					danmuSP.initBMPCache();
				}
				needToAddSpList.add(danmuSP);
			}
			
			long endTime = System.currentTimeMillis();
			L.log("循环用时:" + (endTime - startTime));
			// 刷新全部弹幕位置
			for (DanMakuSprite danMakuSprite : showingDanmuSpList) {
				danMakuSprite.freshSprite(curMills);
			}
	
			// 开始结束一帧的工作
			//从等待弹幕列表移除已经开始播放的弹幕
			//waitingDanMuList.removeAll(needToAddSpList);
			waitingDanMuList.clear();
			// 从弹幕bitmap缓冲池中移除Bitmap
			for (DanMakuSprite sp : needToRemoveSpList) {
				sp.recycleDanMakuSp();
				bitmapManager.removeBitmap(sp.bmpId);
			}
			// 从播放列表移出全部离开屏幕弹幕
			showingDanmuSpList.removeAll(needToRemoveSpList);
			// 添加需要添加的弹幕到播放列表
			showingDanmuSpList.addAll(needToAddSpList);
			// 根据Z轴将当前显示弹幕排序
	//		Collections.sort(showingDanmuSpList,
	//				DanMaKuViewConstants.zOrderComparetor);
			// 获取本次移动的参数信息
			danMakusStatus.showingDanmakuCount = showingDanmuCount;
			danMakusStatus.waitingDanmakuCount = waitingDanMuList.size();
			danMakusStatus.removeDanmakuCount = needToRemoveSpList.size();
			danMakusStatus.addNewDanmakuCount = needToAddSpList.size();
			danMakusStatus.danmubmpCacheCount = bitmapManager.getBmpCacheSize();
			danMakusStatus.curTime = curMills;
			// 清除全部弹幕缓存，共下一帧使用
			needToRemoveSpList.clear();
			needToAddSpList.clear();
	
			long methodEndTime = System.currentTimeMillis();
			// 弹幕移动方法调用计时
			danMakusStatus.moveDanmakusUseTimeMills = methodEndTime - methodStartTime;
		}
		return danMakusStatus;
	}

	/**
	 * 清除缓存中的弹幕，包括将要添加的顶部，滚动弹幕，等待中的顶部，滚动弹幕。供seek视频时使用
	 */
	public void clearDanmuCache() {
		waitingDanMuList.clear();
		needToRemoveSpList.clear();
		needToAddSpList.clear();
		//清除BMP缓冲区的弹幕缓存
	}

	/**
	 * 获取当前显示的全部弹幕
	 * @return
	 */
	public CopyOnWriteArrayList<DanMakuSprite> getShowingDanmakuList(){
		return showingDanmuSpList;
	}
	
	/**
	 * 清除当前显示的全部弹幕
	 */
	public void clearShowingDanmaku() {
		synchronized (freshingDanmakuListLock) {
			for (DanMakuSprite sp : showingDanmuSpList) {
				bitmapManager.removeBitmap(sp.bmpId);
			}
			showingDanmuSpList.clear();
		}
	}

	/**
	 * 修改弹幕管道数量
	 */
	public void changeDanmakuChannelsNum() {
		synchronized (freshingDanmakuListLock) {
			showingDanmuSpList.clear();
			needToAddSpList.clear();
			needToRemoveSpList.clear();
			channelManeger = new ChannelManeger(DanMakuViewSettings.maxColumes);
		}
	}

	/**
	 * 向弹幕队列中添加一条弹幕 ,会在设定的时间播放
	 * @param danMakuSprite
	 * @param isShowRightNow 是否立即播放 注意 立即播放将会修改danmaku对象的播放时间
	 */
	public void addDanmaku(DanMaKu danMaku , boolean isShowRightNow,long curTime){
		DanMakuSprite danMakuSprite = DanMaKuSPFactory.createDanMaKuSP(danMaku);
		if(isShowRightNow == true){
			danMakuSprite.showTimeMS = curTime;
			waitingDanMuList.add(danMakuSprite);
		}
		mDanmakuRetainer.addNewDanmaku(danMakuSprite);
	}
	
	/**
	 * 开启预先缓存，会开启线程进行弹幕的预先载入
	 * @param cacheCount 缓冲池大小
	 */
	public void openCacheThread(int cacheCount){
		
	}

	/**
	 * 添加等待弹幕
	 * 
	 * @param curTimeMills
	 */
	protected void addWaitingDanmaku(long lastAddTime , long curTimeMills) {
			
		ArrayList<DanMakuSprite> preShowingDanmaku = mDanmakuRetainer.sub(lastAddTime, curTimeMills);
		if(preShowingDanmaku == null){
			return;
		}
		for(DanMakuSprite danmuItem : preShowingDanmaku){
			if(mFliterManager.handleDanmakuSP(danmuItem) == false){
				continue;
			}
			waitingDanMuList.add(danmuItem);
		}
	}

	/**
	 * 跳转到特定时间弹幕
	 * 
	 * @param seekTimeMills
	 */
	public void seekDanmakuToTimeMills(long seekTimeMills) {
		// 清除当前缓存弹幕,包括正在显示的弹幕,需要移除的弹幕 , 需要添加的弹幕
		this.clearDanmuCache();
	}

	/**
	 * 每一帧弹幕逻辑计算的状态信息
	 */
	class DanMakusStatus {
		/**
		 * 当前屏幕剩余弹幕数
		 */
		public int showingDanmakuCount;
		/**
		 * 等待弹幕数
		 */
		public int waitingDanmakuCount;
		/**
		 * 本次回收弹幕数量
		 */
		public int removeDanmakuCount;
		/**
		 * 本次新添加弹幕数量
		 */
		public int addNewDanmakuCount;
		/**
		 * 本次弹幕位置计算剩余Bitmap数量
		 */
		public int danmubmpCacheCount;
		/**
		 * 本次绘制开始时间
		 */
		public long curTime;
		/**
		 * 本次绘制用是
		 */
		public long moveDanmakusUseTimeMills;
	}

	/**
	 * 初始化回调
	 * @author 晨晖
	 */
	public interface Callback{
		public void onReady();
	}
}
