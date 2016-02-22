package DanMakuClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import DanmakuSP.DanMakuSprite;
import android.util.Log;

import com.example.javaBean.DanMaKu;

/**
 * 用于管理弹幕缓存
 * @author 晨晖
 *
 */
public class DanmakuRetainer {
	public ArrayList<DanMakuSprite> RTOLDanmuSPArr;
	public ArrayList<DanMakuSprite> TOPDanmuSPArr;
	
	public DanmakuRetainer(){
		RTOLDanmuSPArr = new ArrayList<DanMakuSprite>();
		TOPDanmuSPArr = new ArrayList<DanMakuSprite>();
	}
	
	/**
	 * 根据弹幕数据生成SP列表
	 * @param danmuData
	 */
	public void setPlayDanmmuList(ArrayList<DanMaKu> danmuData){
		Collections.sort(danmuData, new DanMaKuViewConstants.SortByTimeMills());
		for(int i = 0 , size = danmuData.size() ; i < size ; i++){
			DanMaKu tempDanmaku = danmuData.get(i);
			switch(tempDanmaku.getDanmuType()){
				case DanMaKuViewConstants.DANMU_TYPE_RIGHT_TOLEFT:
					RTOLDanmuSPArr.add(DanMaKuSPFactory.createDanMaKuSP(tempDanmaku));
					break;
				case DanMaKuViewConstants.DANMU_TYPE_MUTLI:
					RTOLDanmuSPArr.add(DanMaKuSPFactory.createDanMaKuSP(tempDanmaku));
					break;
				case DanMaKuViewConstants.DANMU_TYPE_TOP:
					TOPDanmuSPArr.add(DanMaKuSPFactory.createDanMaKuSP(tempDanmaku));
					break;
				default:
					break;
			}
		}
	}
	
	/**
	 * 添加一条新的弹幕到列表中
	 * @param danMaKu
	 * @param timeMills
	 */
	public void addNewDanmaku(DanMakuSprite danMaKu){
		ArrayList<DanMakuSprite> addList = null;
		switch(danMaKu.danmakuType){
			case DanMaKuViewConstants.DANMU_TYPE_RIGHT_TOLEFT:
				addList = RTOLDanmuSPArr;
				break;
			case DanMaKuViewConstants.DANMU_TYPE_MUTLI:
				addList = RTOLDanmuSPArr;
				break;
			case DanMaKuViewConstants.DANMU_TYPE_TOP:
				addList = TOPDanmuSPArr;
				break;
			default:
				addList = RTOLDanmuSPArr;
				danMaKu.danmakuType = DanMaKuViewConstants.DANMU_TYPE_RIGHT_TOLEFT;
				break;
		}
		synchronized(addList.getClass()){
			for(int i = 0 ; i < addList.size() ; i++){
				if(addList.get(i).showTimeMS > danMaKu.showTimeMS){
					addList.add(i,danMaKu);
					Log.e("tag","添加序号："+i);
					return;
				}
			}
		}
	}
	
	public ArrayList<DanMakuSprite> sub(long startTimeMills , long endTimeMills){
		ArrayList<DanMakuSprite> resArr = new ArrayList();
		resArr.addAll(this.sub(RTOLDanmuSPArr, startTimeMills,endTimeMills));
		resArr.addAll(this.sub(TOPDanmuSPArr, startTimeMills,endTimeMills));
		return resArr;
	}
	
	/**
	 * 获取子列表
	 * @param list
	 * @param startTimeMills
	 * @param endTimeMills
	 * @return
	 */
	private List<DanMakuSprite> sub(ArrayList<DanMakuSprite> list , long startTimeMills , long endTimeMills){
		synchronized(list.getClass()){
			int startIndex = -1;
			int endIndex= -1;
			for(int i = 0 , size = list.size() ; i < size ; i++){
				if(list.get(i).showTimeMS >= startTimeMills && list.get(i).showTimeMS < endTimeMills){
					if(startIndex == -1){
						startIndex = endIndex =  i;
					}
				}
			}
			for(int i = startIndex+1 , size = list.size() ; i < size ; i++){
				if(list.get(i).showTimeMS >= endTimeMills){
					endIndex = i;
					break;
				}
			}
			if(startIndex == -1){
				return new ArrayList<DanMakuSprite>();
			}
			if(endIndex == -1){
				endIndex = list.size();
			}
			Log.e("tag",String.format("时间为（%s，%s）,区间为（%s,%s）",startTimeMills,endTimeMills,startIndex , endIndex));
			return list.subList(startIndex, endIndex);
		}
	}
}
