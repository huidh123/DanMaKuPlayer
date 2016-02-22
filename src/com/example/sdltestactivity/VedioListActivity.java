package com.example.sdltestactivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.AndroidUtils.L;
import com.example.Utils.BiliDanmakuXMLParser;
import com.example.Utils.Constant;
import com.example.Utils.Constants;
import com.example.Utils.DataUtils;
import com.example.Utils.NetWorkUtils;
import com.example.javaBean.DanMaKu;
import com.example.javaBean.DanmuFileData;

public class VedioListActivity extends Activity{
	private ListView lv_vedio_list;
	private ProgressDialog loadingDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vedio_list);
		initView();
	}
	private void initView(){
		lv_vedio_list= (ListView) findViewById(R.id.lv_vedio_file_list);
		
		loadingDialog = new ProgressDialog(VedioListActivity.this);
        loadingDialog.setMessage("正在加载弹幕文件");
        loadingDialog.setCancelable(false);
        
		ArrayList<String> resList = (ArrayList<String>) getFilesPath();
		String[] fileArr =new String[resList.size()];
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,getFilesPath().toArray(fileArr));
		lv_vedio_list.setAdapter(adapter);
		lv_vedio_list.setOnItemClickListener(new OnVedioItemClickListener());
	}
	private List<String> getFilesPath(){
		String path = "/storage/emulated/0/DCIM/sdl";
		//path = "/storage/sdcard1/";
		File rootFile = new File(path);
		File[] fileArr = rootFile.listFiles();
		ArrayList<String> files = new ArrayList<String>();
		for(File f : fileArr){
			if(f.isFile()){
				files.add(f.getAbsolutePath());
			}
		}
		return files;
	}

	class OnVedioItemClickListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			NetGetDanmakuFileTask netGetDanmakuFileTask = new NetGetDanmakuFileTask(5044688);
            netGetDanmakuFileTask.execute();
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
            loadingDialog.show();
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
            loadingDialog.dismiss();
            if (danmuFileData == null) {
                Log.e("tag", "弹幕解析失败");
            } else {
                Log.e("tag", "弹幕服务器" + danmuFileData.getChatServer());
                Log.e("tag", "弹幕数量" + danmuFileData.getDanmuList().size());
                Constants.danmuList = danmuFileData.getDanmuList();
                //Constants.danmuList = getTestList();
                Collections.sort(danmuFileData.getDanmuList(), new SortByTimeDesc());
                //设置弹幕到全局变量处，准备播放

                startActivity(new Intent(VedioListActivity.this, GiliGiliPlayActivity.class));
            }
        }
    }
    

    class SortByTimeDesc implements Comparator<DanMaKu> {

        @Override
        public int compare(DanMaKu danMaKu, DanMaKu t1) {

            return danMaKu.getShowTime() < t1.getShowTime() ? -1 : 1;
        }
    }
}
