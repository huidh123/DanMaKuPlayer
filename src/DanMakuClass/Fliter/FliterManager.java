package DanMakuClass.Fliter;

import java.util.LinkedList;
import java.util.List;

import DanmakuSP.DanMakuSprite;
import android.util.Log;

public class FliterManager {

	private List<IDanmakuFliter> mDanmakuFliterList;
	private FliterContext context;
	public FliterManager(FliterContext context){
		mDanmakuFliterList = new LinkedList<IDanmakuFliter>();
		this.context = context;
	}
	
	public void addFliter(IDanmakuFliter danmakuFliter){
		mDanmakuFliterList.add(danmakuFliter);
	}
	
	public boolean  handleDanmakuSP(DanMakuSprite danMakuSprite){
		//Log.e("FliterManager" , "FliterManager" + "判断弹幕:"+danMakuSprite.message);
		for(int i = 0 ; i < mDanmakuFliterList.size() ; i++){
			if(mDanmakuFliterList.get(i).handleFliter(context, danMakuSprite) == false){
				Log.e("FliterManager" , "FliterManager" + "结果:False");
				return false;
			}
		}
		return true;
	}
}
