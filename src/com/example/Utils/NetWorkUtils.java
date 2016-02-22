package com.example.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.DeflateInputStream;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.example.AndroidUtils.L;
import com.google.gson.Gson;


/**
 * Created by 晨晖 on 2015-06-30.
 */
public class NetWorkUtils {

	public static Gson gson = new Gson();
	
    private static String tag = "NetWorkUtils";
    /**
     * 网络访问使用的client
     */
    public static HttpClient httpClient = NetWorkUtils.getHttpClient();;

    /**
     * 连接超时时间
     */
    public static int CONNECT_TIMEOUT = 7000;

    /**
     * 数据读取超时时间
     */
    public static int READ_DAT_TIME_OUT = 12000;

    /**
     * 请求服务器with session
     *
     * @param url
     * @param session session
     * @return
     * @throws SocketTimeoutException
     */
    public static String getStrFromUrl(String url) throws SocketTimeoutException{
        HttpGet httpGet = new HttpGet(url.trim());
        int resCode = 0;
        String resStr = null;
        try {
            HttpResponse httpResponse = NetWorkUtils.httpClient.execute(httpGet);
            resCode = httpResponse.getStatusLine().getStatusCode();
            resStr = EntityUtils.toString(httpResponse.getEntity());
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
            throw new SocketTimeoutException();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (resCode >= 400) {
            return null;
        }
        return resStr;
    }


    /**
     * 下载弹幕文件缓存到本地
     * @param chatId
     * @return
     */
    public static boolean downLoadDanmuFile(int chatId) throws SocketTimeoutException {
        String danmuFileUrl = String.format(Constant.GET_CHAT_FILE_SERVER_URL,chatId);
        String DanmuSaveFilePath = Constant.DANMU_FILE_SAVE_PATH+chatId;
        L.e("访问URL"+danmuFileUrl);
        File danmuFileSet = new File(Constant.DANMU_FILE_SAVE_PATH);
        if(!danmuFileSet.exists()){
            danmuFileSet.mkdirs();
        }
        File danmuFile = new File(DanmuSaveFilePath);
        L.e(danmuFile.getPath());
        if(!danmuFile.exists()){
            try {
                danmuFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        HttpURLConnection httpConnection = null;
        DeflateInputStream deflaterInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            URL tempUrl = new URL(danmuFileUrl);
            httpConnection = (HttpURLConnection) tempUrl.openConnection();
            httpConnection.setReadTimeout(CONNECT_TIMEOUT);
            httpConnection.setConnectTimeout(READ_DAT_TIME_OUT);
            httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36 LBBROWSER");
            httpConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpConnection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
            httpConnection.setRequestProperty("Accept-Encoding", "gzip");
            deflaterInputStream = new org.apache.http.client.entity.DeflateInputStream(httpConnection.getInputStream());
            fileOutputStream = new FileOutputStream(danmuFile);

            byte [] buffer = new byte[512];
            int length = 0;
            int totalLength = 0;
            while((length = deflaterInputStream.read(buffer)) != -1){
                fileOutputStream.write(buffer,0,length);
                totalLength+= length;
            }
            fileOutputStream.flush();
        }catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectException e) {
            throw new SocketTimeoutException();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            IOUtils.closeQuietly(fileOutputStream);
            IOUtils.closeQuietly(deflaterInputStream);
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
        return true;
    }
    
    
    public static synchronized HttpClient getHttpClient() {
        if (null == httpClient) {
            HttpParams params = new BasicHttpParams();
            // 设置一些基本参数
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            HttpProtocolParams.setUseExpectContinue(params, true);
            HttpProtocolParams.setUserAgent(params, "Mozilla/5.0(Linux;U;Android 2.2.1;en-us;Nexus One Build.FRG83) " + "AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1");
            // 超时设置
                        /* 从连接池中取连接的超时时间 */
            ConnManagerParams.setTimeout(params, 1000);
                        /* 连接超时 */
            HttpConnectionParams.setConnectionTimeout(params, NetWorkUtils.CONNECT_TIMEOUT * 1000);
                        /* 请求超时 */
            HttpConnectionParams.setSoTimeout(params, NetWorkUtils.READ_DAT_TIME_OUT * 1000);

            // 设置我们的HttpClient支持HTTP和HTTPS两种模式
            SchemeRegistry schReg = new SchemeRegistry();
            schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

            // 使用线程安全的连接管理来创建HttpClient
            ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);
            httpClient = new DefaultHttpClient(conMgr, params);
        }
        return httpClient;
    }
}
