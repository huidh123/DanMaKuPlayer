package org.libsdl.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import android.os.Environment;
import android.util.Log;


public class Utils {
	public static String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// ��ȡ��Ŀ¼
		}
		return sdDir.toString();
	}

	public static char[] getChars(byte[] bytes) {
		Charset cs = Charset.forName("UTF-8");
		ByteBuffer bb = ByteBuffer.allocate(bytes.length);
		bb.put(bytes);
		bb.flip();
		CharBuffer cb = cs.decode(bb);
		return cb.array();
	}
	
	public static  String getPath2() {
	    String sdcard_path = null;
	    String sd_default = Environment.getExternalStorageDirectory()
	        .getAbsolutePath();
	    Log.d("text", sd_default);
	    if (sd_default.endsWith("/")) {
	      sd_default = sd_default.substring(0, sd_default.length() - 1);
	    }
	    // 得到路径
	    try {
	      Runtime runtime = Runtime.getRuntime();
	      Process proc = runtime.exec("mount");
	      InputStream is = proc.getInputStream();
	      InputStreamReader isr = new InputStreamReader(is);
	      String line;
	      BufferedReader br = new BufferedReader(isr);
	      while ((line = br.readLine()) != null) {
	        if (line.contains("secure"))
	          continue;
	        if (line.contains("asec"))
	          continue;
	        if (line.contains("fat") && line.contains("/mnt/")) {
	          String columns[] = line.split(" ");
	          if (columns != null && columns.length > 1) {
	            if (sd_default.trim().equals(columns[1].trim())) {
	              continue;
	            }
	            sdcard_path = columns[1];
	          }
	        } else if (line.contains("fuse") && line.contains("/mnt/")) {
	          String columns[] = line.split(" ");
	          if (columns != null && columns.length > 1) {
	            if (sd_default.trim().equals(columns[1].trim())) {
	              continue;
	            }
	            sdcard_path = columns[1];
	          }
	        }
	      }
	    } catch (Exception e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    }
	    Log.d("text", sdcard_path);
	    return sdcard_path;
	  }
	
}
