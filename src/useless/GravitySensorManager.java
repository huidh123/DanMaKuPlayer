package useless;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;


public class GravitySensorManager {

	private SensorManager senserManager;
	private Sensor gravitySensor;
	private Context context;
	private OnGravityChangeListener changeListener;
	private boolean isStartWatching = false;
	//初始朝向向量
	public float startVecX = -1;
	public float startVecY = -1;
	public float startVecZ = -1;
	//当前朝向方向向量
	public float curVecX = -1;
	public float curVecY = -1;
	public float curVecZ = -1;
	private float MAX_RADUIS = 10;

	public GravitySensorManager(Context context,
			OnGravityChangeListener changeListener) {
		this.changeListener = changeListener;
		senserManager = (SensorManager) context
				.getSystemService(context.SENSOR_SERVICE);
		gravitySensor = senserManager.getSensorList(Sensor.TYPE_ACCELEROMETER)
				.get(0);
		senserManager.registerListener(new OnSensorChangeListener(),
				gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void startWatching() {
		isStartWatching = true;
	}

	class OnSensorChangeListener implements SensorEventListener {

		int temp = 0;
		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			temp++;
			if(temp % 3 != 0){
				return;
			}
			if (isStartWatching) {
				curVecX = event.values[SensorManager.DATA_X];
				curVecY = event.values[SensorManager.DATA_Y];
				curVecZ = event.values[SensorManager.DATA_Z];
				if (startVecX == -1) {
					startVecX = curVecX;
					startVecY = curVecY;
					startVecZ = curVecZ;
				}
				float curAngle = caculateAngle(curVecX, curVecY, curVecZ);
				
				float curRate = caculateK(startVecX , startVecY , startVecZ , curVecX, curVecY ,curVecZ);
				changeListener.OnGravityChanged(curAngle, curRate);
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}
	}

	/**
	 * 计算重力方向角度
	 * 
	 * @return
	 */
	private float caculateAngle(float x, float y, float z) {

		float res1 = (x * startVecX) + (y * startVecY) + (z * startVecZ);
		float res2 = (float) (Math.sqrt(x * x + y * y + z * z) + Math
				.sqrt(startVecX * startVecX + startVecY * startVecY + startVecZ
						* startVecZ));
		//Log.e("raduis", String.format("res1 = %s , res2 = %s", res1, res2));
		return (res1 / res2);
	}

	private float caculateK(float x1, float y1, float z1, float x2, float y2,
			float z2) {
		float k = ((x1 * x2 + y1 * y2 + z1 * z2) / (x1 * x1 + y1 * y1 + z1 * z1));
		float vec1X = (x2 - x1 * k);
		float vec1Y = (y2 - y1 * k);
		float vec1Z = (z2 - z1 * k);

		float k2 = (startVecX * startVecX + startVecY * startVecY + startVecZ * startVecZ);
		float vec2X = -startVecX;
		float vec2Y = -startVecY ;
		float vec2Z = k2 - startVecZ;
		
		float res1 = (vec1X * vec2X) + (vec1Y * vec2Y) + (vec1Z * vec2Z);
		float res2 = (float) (Math.sqrt(vec1X * vec1X + vec1Y * vec1Y + vec1Z
				* vec1Z) * Math.sqrt(vec2X * vec2X + vec2Y * vec2Y + vec2Z
				* vec2Z));
		
		Log.e("angle",String.format("vecMain :::::: x = %s , y= %s , z = %s", vec2X , vec2Y , vec2Y));
		Log.e("angle",String.format("vec1 :::::: x = %s , y= %s , z = %s", vec1X , vec1Y , vec1Y));
		Log.e("rate", String.format("res1 = %s , res2 = %s", res1, res2));
		return (res1 / res2);
	}
}
