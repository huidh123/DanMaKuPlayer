package DanMakuClass;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 晨晖 on 2015-07-31.
 */
public class ChannelManeger {

    private Map<Integer , boolean []> channelMap;
    
    private int columes;

    public ChannelManeger(int columes){
        this.columes = columes;
        boolean [] RtoLChannels = new boolean[columes];
        boolean [] TOPChannels = new boolean[columes];
        channelMap = new HashMap<Integer, boolean[]>();
        //添加多种弹幕格式
        channelMap.put(DanMaKuViewConstants.DANMU_TYPE_RIGHT_TOLEFT, RtoLChannels);
        channelMap.put(DanMaKuViewConstants.DANMU_TYPE_TOP, TOPChannels);
    }

    /**
     * 返回空闲管道序号,无空闲则返回-1
     * @param danmuType
     * @return
     */
    public int getFreeColumeIndex(int danmuType , int channelNum){
    	//Log.e("getFreeColumeIndex",String.format("弹幕宽度:%s", channelNumber));
        boolean [] channel = channelMap.get(danmuType);
        if(channel == null){
        	channel = channelMap.get(DanMaKuViewConstants.DANMU_TYPE_RIGHT_TOLEFT);
        }
        
        for(int i = 0 ; i < (channel.length - channelNum) ; i++){
        	int tempIndex = 0;
        	if(channel[i] == false){
        		tempIndex = i;
        	}else{
        		continue;
        	}
        	for(int j = tempIndex ; j < (tempIndex + channelNum) ; j++){
        		if(channel[j] == true){
        			continue;
        		}
        	}
        	return tempIndex;
        }
        return -1;
    }

    /**
     * 设置某个管道是否被占用
     * @param danmuType
     * @param index
     */
    public void setIsChannelUsed(int danmuType , int index ,int number ,  boolean isUse){
    	 boolean [] channel = channelMap.get(danmuType);
         if(channel == null){
         	channel = channelMap.get(DanMaKuViewConstants.DANMU_TYPE_RIGHT_TOLEFT);
         }
        if(index < 0 || (index + number) > channel.length){
        	return;
        }
        for(int i = index ; i < (index +number) ; i++){
        	channel[i] = isUse;
        }
    }

    /**
     * 清除所有管道占用
     */
    public void clearAllChannelUser(){
    	for(Map.Entry<Integer, boolean []> entry : channelMap.entrySet()){
    		entry.setValue(new boolean[this.columes]);
    	}
    }
    
    public String toString(){
    	String res = "";
    	res += "宽度:"+this.columes;
		return res;
    }
}
