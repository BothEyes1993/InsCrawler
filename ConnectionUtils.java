package com.sbcm.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class ConnectionUtils {
	private static final Logger logger = LoggerFactory.getLogger(ConnectionUtils.class);
	/**
	 * 
	 * @param urlAll:请求接口
	 * @param charset:字符编码
	 * @return 返回json结果
	 */
	public static String get(String urlAll, String charset) {
		BufferedReader reader = null;
		String result = null;
		StringBuffer sbf = new StringBuffer();
		String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";// 模拟浏览器
		try {
			URL url = new URL(urlAll);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setReadTimeout(30000);
			connection.setConnectTimeout(30000);
			connection.setRequestProperty("User-agent", userAgent);
			connection.connect();
			InputStream is = connection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(is, charset));
			String strRead = null;
			while ((strRead = reader.readLine()) != null) {
				sbf.append(strRead);
				sbf.append("\r\n");
			}
			
			result = sbf.toString();
			if(200!=connection.getResponseCode()){
				throw new Exception("connection failed:"+result);
			}
		} catch (Exception e) {
			logger.error(urlAll,e);
		}finally {
			if(reader!=null){
				try {
					reader.close();
				} catch (IOException e) {
					logger.error("IO close exception",e);
				}
			}
		}
		return result;
	}

	public static void main(String[] args) {
		String r = ConnectionUtils.get(
				"https://www.instagram.com/kaikaiko/",
				"utf-8");
		System.out.println(r);
	}
}
