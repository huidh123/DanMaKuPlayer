package DanMakuClass.Fliter;

import DanMakuClass.DanMaKuManeger;


/**
 * 过滤器上下文类
 * @author 晨晖
 *
 */
public class FliterContext {

	private DanMaKuManeger mDanmakuManager;
	
	public FliterContext(DanMaKuManeger danMaKuManeger){
		this.mDanmakuManager = danMaKuManeger;
	}

	/**
	 * 获取当前显示弹幕数量
	 * @return
	 */
	public int getShowingDanmakuCount(){
		return mDanmakuManager.getShowingDanmakuList().size();
	}
}
