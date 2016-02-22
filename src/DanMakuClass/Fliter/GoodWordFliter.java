package DanMakuClass.Fliter;

import DanmakuSP.DanMakuSprite;

public class GoodWordFliter extends IDanmakuFliter {

	private String mGoodWord;
	
	public GoodWordFliter(String goodWord) {
		this.mGoodWord = goodWord;
	}
	
	@Override
	protected boolean handleFliter(FliterContext context,
			DanMakuSprite danMakuSprite) {
		if(danMakuSprite.message == null || danMakuSprite.message instanceof String){
			return true;
		}
		if(danMakuSprite.message.contains(mGoodWord)){
			return false;
		}
		return true;
	}

}
