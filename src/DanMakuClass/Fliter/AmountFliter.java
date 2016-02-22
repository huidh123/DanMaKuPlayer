package DanMakuClass.Fliter;

import DanmakuSP.DanMakuSprite;

public class AmountFliter extends IDanmakuFliter{
	
	@Override
	protected boolean handleFliter(FliterContext context,
			DanMakuSprite danMakuSprite) {
		if(context.getShowingDanmakuCount() >= 100){
			return false;
		}
		return true;
	}

}
