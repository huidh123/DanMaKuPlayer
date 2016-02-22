package useless;

/**
 * 视差变化监听器
 * @author 晨晖
 *
 */
public interface OnGravityChangeListener {
	/**
	 * 视差变化返回接口，返回角度与标准角度差距
	 * @param degree 返回倾斜程度[-5 , 5] 5为完全没有倾斜，0为垂直原方向 ， 负数代表反转
	 */
	public void OnGravityChanged(float degree , float changeAngle);
}
