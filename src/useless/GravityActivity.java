package useless;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.sdltestactivity.R;

public class GravityActivity extends Activity {

	private TextView textView;
	private TextView tv_angle;
	GravitySensorManager gravitySensorManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gravity);
		textView = (TextView) findViewById(R.id.tv_gravity);
		tv_angle = (TextView) findViewById(R.id.tv_angle);
		
		gravitySensorManager = new GravitySensorManager(this, new OnGravityListener());
		gravitySensorManager.startWatching();
		
	}
	
	class OnGravityListener implements OnGravityChangeListener{

		@Override
		public void OnGravityChanged(float rate, float changeAngle) {
			// TODO Auto-generated method stub
			textView.setText("位移距离："+rate + "\n位移角度："+changeAngle);
			tv_angle.setText(String.format("记录角度（%s,%s,%s）\n当前角度（%s,%s ,%s）", gravitySensorManager.startVecX , gravitySensorManager.startVecY , gravitySensorManager.startVecZ , gravitySensorManager.curVecX , gravitySensorManager.curVecY , gravitySensorManager.curVecZ));
		}
		
	}

}
