package com.example.sdltestactivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import DanMakuClass.DanMaKuViewConstants;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cch.floatplugs.FloatWinService;
import com.cch.floatplugs.FloatingDanmakuViewService;
import com.example.AndroidUtils.L;
import com.example.AndroidUtils.T;
import com.example.Utils.BiliDanmakuXMLParser;
import com.example.Utils.Constant;
import com.example.Utils.Constants;
import com.example.Utils.DataUtils;
import com.example.Utils.NetWorkUtils;
import com.example.javaBean.AVBaseInfo;
import com.example.javaBean.AVVedioPath;
import com.example.javaBean.BMPString;
import com.example.javaBean.DanMaKu;
import com.example.javaBean.DanmuFileData;

public class SearchActivity extends Activity {

	private EditText et_av_num_input;
	private TextView tv_vedio_info;
	private Button btn_play_start;
	private Button btn_open_plugs;
	
	private SearchActivity.OnGetVedioInfoTask task;

	private ProgressDialog LoadingVedioInfoDialog;
	private ProgressDialog GetDanmakuDialog;
	private AVVedioPath getVedioDeatials;
	private AVBaseInfo getVedioinfo;
	private boolean mIsPlugsOpen = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_input_av);

		et_av_num_input = (EditText) findViewById(R.id.et_av_num_input);
		tv_vedio_info = (TextView) findViewById(R.id.tv_vedio_info);
		btn_play_start = (Button) findViewById(R.id.btn_play_start);
		btn_open_plugs = (Button) findViewById(R.id.btn_open_plugs);
		
		LoadingVedioInfoDialog = new ProgressDialog(this);
		LoadingVedioInfoDialog.setTitle("正在加载视频地址");
		LoadingVedioInfoDialog.setCancelable(false);
		GetDanmakuDialog = new ProgressDialog(this);
		GetDanmakuDialog.setTitle("正在加载弹幕文件");
		GetDanmakuDialog.setCancelable(false);
		
		btn_play_start.setEnabled(false);
		btn_play_start.setText("请输入AV号");
		et_av_num_input.addTextChangedListener(new OnAVInputTextListener());
		btn_play_start.setOnClickListener(new OnPlayClickListener());
		
		btn_open_plugs.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mIsPlugsOpen == false){
					Log.e("tag","炫富窗開啟");
					mIsPlugsOpen = true;
					startService(new Intent(SearchActivity.this , FloatWinService.class));
				}else{
					Log.e("tag","炫富窗關閉");
					mIsPlugsOpen = false;
					stopService(new Intent(SearchActivity.this , FloatWinService.class));
					stopService(new Intent(SearchActivity.this , FloatingDanmakuViewService.class));
				}
			}
		});

	}

	private void initVedioInfoTextView(AVBaseInfo result){
		String vedioInfoText = 
				String.format("视频类型：%s\n播放次数：%d\n重播次数：%d\n视频重播次数：%d\n收藏次数：%d\n视频标题：%s\n视频标签：%s\n视频投稿：%s\n硬币数：%d\n上传时间：%s",
						result.getTypename(),
						result.getPlay(),
						result.getReview() ,
						result.getVideo_review(),
						result.getFavorites() , 
						result.getTitle(),
						result.getTag(),
						result.getAuthor(),
						result.getCoins(),
						result.getCreated_at());
		tv_vedio_info.setText(vedioInfoText);
	}

	class OnPlayClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!TextUtils.isEmpty(et_av_num_input.getText())){
				GetVedioPathTask getVedioPathTask = new GetVedioPathTask(et_av_num_input.getText().toString());
				getVedioPathTask.execute();
			}
		}
	}
	
	class OnAVInputTextListener implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub

		}

		@Override
		public void afterTextChanged(Editable s) {
			Log.e("tag","数据编辑");
			// TODO Auto-generated method stub
			if(TextUtils.isEmpty(s.toString())){
				Log.e("tag","数据为空");
				if(task != null){
					task.cancel(true);
				}
				return;
			}
			if(task == null){
				task = new OnGetVedioInfoTask(s.toString());
				task.execute();
			}else if(!task.isCancelled() && !s.toString().equals(task.aid)){
				if(task != null){
					task.cancel(true);
				}
				task = new OnGetVedioInfoTask(s.toString());
				task.execute();
			}else{
				task = new OnGetVedioInfoTask(s.toString());
				task.execute();
			}
		}
	}

	class OnGetVedioInfoTask extends AsyncTask<Void, Void, AVBaseInfo> {

		public String aid;

		public OnGetVedioInfoTask(String aid) {
			this.aid = aid;
		}

		@Override
		protected AVBaseInfo doInBackground(Void... params) {
			// TODO Auto-generated method stub
			String res = null;

			try {
				res = NetWorkUtils.getStrFromUrl(String.format(
						Constant.GET_VEDIO_INFO_BY_AVNUM, aid));
			} catch (SocketTimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			Log.e("tag","查询信息："+res);
			AVBaseInfo vedioInfo = NetWorkUtils.gson.fromJson(res,AVBaseInfo.class);
			if(vedioInfo == null || vedioInfo.getCid() == null){
				return null;
			}
			return vedioInfo;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			tv_vedio_info.setText("正在查询信息");
			btn_play_start.setEnabled(false);
			btn_play_start.setText("请等待");
		}

		@Override
		protected void onPostExecute(AVBaseInfo result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			getVedioinfo = result;
			if(result != null){
				initVedioInfoTextView(result);
				btn_play_start.setEnabled(true);
				btn_play_start.setText("开始播放");
			}else{
				tv_vedio_info.setText("未查询到视频信息");
				btn_play_start.setEnabled(false);
				btn_play_start.setText("无法播放");
			}
		}
	}
	
	class GetVedioPathTask extends AsyncTask<Void, Void, AVVedioPath>{

		private String aid;
		
		public  GetVedioPathTask(String aid) {
			this.aid = aid;
		}
		@Override
		protected AVVedioPath doInBackground(Void... params) {
			String res = null;
			
			try {
				res = NetWorkUtils.getStrFromUrl(String.format(Constant.GET_VEDIO_PATH , aid));
			} catch (SocketTimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			
			AVVedioPath avVedioPath = NetWorkUtils.gson.fromJson(res, AVVedioPath.class);
			return avVedioPath;
		}
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			LoadingVedioInfoDialog.show();
		}
		@Override
		protected void onPostExecute(AVVedioPath result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			LoadingVedioInfoDialog.dismiss();
			if(result != null){
				getVedioDeatials = result;
				NetGetDanmakuFileTask danmuTask = new NetGetDanmakuFileTask(getVedioinfo.getCid());
				danmuTask.execute();
			}else{
				T.showLong(SearchActivity.this, "获取视频信息失败");
			}
		}
	}
	
	/**
     * 获取弹幕文件异步任务
     */
    class NetGetDanmakuFileTask extends AsyncTask<Void, Void, DanmuFileData> {
        private int chatId;

        private String resStr;

        public NetGetDanmakuFileTask(int chatId) {
            this.chatId = chatId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            GetDanmakuDialog.show();
        }

        @Override
        protected DanmuFileData doInBackground(Void... voids) {
            Boolean isSuccessed = false;

            try {
                isSuccessed = NetWorkUtils.downLoadDanmuFile(chatId);
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                return null;
            }
            L.e("网络请求成功" + isSuccessed);
            if (!isSuccessed) {
                return null;
            }

            File danmuFile = new File(Constant.DANMU_FILE_SAVE_PATH + chatId);
            if (!danmuFile.exists()) {
                return null;
            }
            L.e("文件解析成功");
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(danmuFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                L.e("文件流读取失败");
                return null;
            }
            L.e("文件流读取成功");
            BiliDanmakuXMLParser biliDanmakuXMLParser = (BiliDanmakuXMLParser) DataUtils.parseXMLToDanmakuFile(fileInputStream, new BiliDanmakuXMLParser());
            return biliDanmakuXMLParser.getDanmakuFileData();
        }

        @Override
        protected void onPostExecute(DanmuFileData danmuFileData) {
            super.onPostExecute(danmuFileData);
            GetDanmakuDialog.dismiss();
            if (danmuFileData == null) {
                Log.e("tag", "弹幕解析失败");
            } else {
                Log.e("tag", "弹幕服务器" + danmuFileData.getChatServer());
                Log.e("tag", "弹幕数量" + danmuFileData.getDanmuList().size());
                Constants.danmuList = danmuFileData.getDanmuList();
                //Constants.danmuList = getTestList();
                Collections.sort(danmuFileData.getDanmuList(), new SortByTimeDesc());
                
                //danmaKindTestMedthod();
                //danmakindTestMethod2();
                //设置弹幕到全局变量处，准备播放
                Intent intent = new Intent(SearchActivity.this, GiliGiliPlayActivity.class);
                intent.putExtra("VedioPath", getVedioDeatials.getSrc());
                startActivity(intent);
            }
        }
    }
    

    class SortByTimeDesc implements Comparator<DanMaKu> {

        @Override
        public int compare(DanMaKu danMaKu, DanMaKu t1) {

            return danMaKu.getShowTime() < t1.getShowTime() ? -1 : 1;
        }
    }
    
    
    /**
     * 多类型弹幕测试
     */
    public void danmaKindTestMedthod(){
    	for(int i = 20 ; i < 100 ; i ++){
    		if(i % 4 == 0){
    			Constants.danmuList.get(i).setDanmakuTextSize(40);
    			Constants.danmuList.get(i).setDanmakuContent("测试多行弹幕\n测试多行弹幕\n测试多行弹幕\n");
    			Constants.danmuList.get(i).setDanmuType(DanMaKuViewConstants.DANMU_TYPE_MUTLI);
    		}
    	}
    }
    
    public void danmakindTestMethod2(){
    	String [] strArr = new String[]{"图文弹幕测试" ,"图文弹幕测试" , "图文弹幕测试" };
    	ArrayList<Bitmap> bmpArr = new ArrayList<Bitmap>();
    	Bitmap tempBmp = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_launcher);
    	bmpArr.add(tempBmp);
    	bmpArr.add(tempBmp);
    	BMPString bmpString = new BMPString(strArr, bmpArr);
    	for(int i = 101 ; i < 200 ; i ++){
    		if(i % 4 == 0){
    			Constants.danmuList.get(i).setDanmakuTextSize(40);
    			Constants.danmuList.get(i).setDanmakuContent(bmpString);
    			Constants.danmuList.get(i).setDanmuType(DanMaKuViewConstants.DANMU_TYPE_BMP);
    		}
    	}
    }
}
