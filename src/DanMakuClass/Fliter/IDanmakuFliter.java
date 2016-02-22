package DanMakuClass.Fliter;

import DanmakuSP.DanMakuSprite;

/**
 * 弹幕过滤器接口
 * @author 晨晖
 *
 */
public abstract class IDanmakuFliter {
	/**
	 * 下一过滤器
	 */
	private FliterContext context;
	
	/**
	 * 判断函数 返回true意味通过过滤器 , false不通过,弹幕被过滤
	 * @param context
	 * @param danMakuSprite
	 * @return
	 */
	protected abstract boolean handleFliter(FliterContext context , DanMakuSprite danMakuSprite);
	
	public void setFliterContext(FliterContext context){
		this.context = context;
	}
}
