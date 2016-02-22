package com.example.customview;

import DanMakuClass.DanMaKuView;
import DanMakuClass.DanMakuViewSettings;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.sdltestactivity.R;

public class PopWinDanmakuSetting extends PopupWindow {

	private Context context;
	private View contentView;
	private TextView tv_font_size;
	private SeekBar sb_font_size;
	private TextView tv_stroke;
	private SeekBar sb_stroke;
	private TextView tv_colume;
	private SeekBar sb_colume;
	private TextView tv_speed;
	private SeekBar sb_speed;
	private TextView tv_alpha;
	private SeekBar sb_alpha;
	private DanMaKuView danMaKuView;
	
	
	private int minFontsSize = 25;
	private int maxFontsSize = 125;
	
	public PopWinDanmakuSetting(Context context , DanMaKuView danMaKuView ){
		contentView = View.inflate(context, R.layout.popwin_danmaku_setting, null);
		this.setHeight(800);
		this.setWidth(750);
		this.setFocusable(true);
		this.setBackgroundDrawable(new BitmapDrawable());
		this.setContentView(contentView);
		
		this.danMaKuView = danMaKuView;
		tv_font_size  =(TextView) contentView.findViewById(R.id.tv_font_size);
		sb_font_size = (SeekBar) contentView.findViewById(R.id.sb_font_size);
		tv_stroke  =(TextView) contentView.findViewById(R.id.tv_stroke);
		sb_stroke = (SeekBar) contentView.findViewById(R.id.sb_stroke);
		tv_colume  =(TextView) contentView.findViewById(R.id.tv_colume);
		sb_colume = (SeekBar) contentView.findViewById(R.id.sb_colume);
		tv_speed  =(TextView) contentView.findViewById(R.id.tv_speed);
		sb_speed = (SeekBar) contentView.findViewById(R.id.sb_speed);
		tv_alpha  =(TextView) contentView.findViewById(R.id.tv_alpha);
		sb_alpha = (SeekBar) contentView.findViewById(R.id.sb_alpha);
		
		tv_font_size.setText(DanMakuViewSettings.DANMAKU_TEXT_SIZE+"");
		sb_font_size.setProgress(DanMakuViewSettings.DANMAKU_TEXT_SIZE+minFontsSize);
		sb_font_size.setMax(maxFontsSize - minFontsSize);
		sb_font_size.setOnSeekBarChangeListener(new OnFontSizeChangeListener());
		
		sb_alpha.setProgress((int) (DanMakuViewSettings.ALPHA_RATE * 100));
		sb_alpha.setMax(100);
		tv_stroke.setText(DanMakuViewSettings.DANMAKU_STROKE_WIDTH+"");
		sb_stroke.setProgress(DanMakuViewSettings.DANMAKU_STROKE_WIDTH);
		tv_colume.setText(DanMakuViewSettings.maxColumes +"行");
		sb_colume.setProgress(DanMakuViewSettings.maxColumes - 3);
		sb_colume.setMax(15);
		
		sb_alpha.setOnSeekBarChangeListener(new OnAlphaChangeListener());
		sb_speed.setOnSeekBarChangeListener(new OnSpeedChangeListener());
		sb_colume.setOnSeekBarChangeListener(new OnChannelNumChangeListener());
		sb_stroke.setOnSeekBarChangeListener(new OnStrokeWidthListener());
	}
	
	
	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		// TODO Auto-generated method stub
		super.showAtLocation(parent, gravity, x, y);
		tv_alpha.setText(danMaKuView.getDanmakuAlpha()+"");
		sb_alpha.setProgress((int) (danMaKuView.getDanmakuAlpha() * 100));
		tv_speed.setText(danMaKuView.getDanmakuSpeed()+"");
		sb_speed.setProgress((int) ((danMaKuView.getDanmakuSpeed() - 0.5) / 2.5 * 100.0f));
	}


	class OnFontSizeChangeListener implements OnSeekBarChangeListener{

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			tv_font_size.setText(progress+minFontsSize+"");
			danMaKuView.setDanmakuFontsSize(progress+minFontsSize);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OnAlphaChangeListener implements OnSeekBarChangeListener{

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			tv_alpha.setText(progress / 100.0f + "");
			danMaKuView.setDanmakuAlpha(progress / 100.0f);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}
	}
	
	class OnSpeedChangeListener implements OnSeekBarChangeListener{

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			if(progress == 100){
				tv_speed.setTextColor(Color.RED);
			}else{
				tv_speed.setTextColor(Color.WHITE);
			}
			tv_speed.setText((0.5 + (2.5 * progress / 100.0f)) + "");
			danMaKuView.setDanmakuSpeed((float) (0.5 + (2.5 * progress / 100.0f)));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OnStrokeWidthListener implements OnSeekBarChangeListener{

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			tv_stroke.setText(progress+"");
			danMaKuView.setDanmakuStrokeWitdh(progress);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			
		}
	}
	
	class OnChannelNumChangeListener implements OnSeekBarChangeListener{

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			tv_colume.setText(progress + 3 + "行");
			danMaKuView.setDanmakuChannelNum(progress+3);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
