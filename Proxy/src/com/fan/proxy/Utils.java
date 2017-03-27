package com.fan.proxy;
import java.text.SimpleDateFormat;

public class Utils {

	static Configer conf = Configer.getInstance();

	static final String METHOD_GET = "GET";
	static final String METHOD_POST = "POST";
	static final String METHOD_CONNECT = "CONNECT";

	static final String AUTHORED = "HTTP/1.1 200 Connection Established\r\n\r\n";
	static final String SERVERERROR = "HTTP/1.1 500 Connection FAILED\r\n\r\n";
	
	static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	static boolean isEmpty(String str) {
		if (str != null && !str.isEmpty()) {
			return false;
		}
		return true;
	}
	
	/**
	 * 避免多线程竞争把日志打串行了
	 */
	public static synchronized void logRequestMsg(String msg) {
		System.out.println(msg);
	}
}