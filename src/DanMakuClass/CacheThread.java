//package DanMakuClass;
//
//import java.util.ArrayList;
//
//import DanmakuSP.DanMakuSprite;
//
///**
// * 用于弹幕预先载入的线程
// * @author 晨晖
// */
//class CacheThread extends SuspendThread{
//	private ArrayList<DanMakuSprite> danmakuList;
//	private BitmapManager mBitmapManager;
//	private long maxCacheCount;
//	
//	
//	public CacheThread(ArrayList<DanMakuSprite> danmuSPList , long maxCacheCount){
//		this.mBitmapManager = BitmapManager.getInstance();
//		this.danmakuList = danmuSPList;
//		this.maxCacheCount = maxCacheCount;
//	}
//	
//	public void setCurrent
//	@Override
//	public void runLogic() {
//		for(DanMakuSprite item : danmakuList){
//			if(mBitmapManager.getBmpCacheSize() < maxCacheCount){
//				if(!item.isInitCache()){
//					item.initBMPCache();
//				}
//			}else{
//				return;
//			}
//		}
//	}
//}