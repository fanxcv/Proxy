package com.fan.proxy;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SocketProxy {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		if (args.length == 2 && "-f".equalsIgnoreCase(args[0])) {
			readConf(true, args[1]);
		} else {
			readConf(false, "c.conf");
		}
		Configer conf = Configer.getInstance();
		ServerSocket serverSocket = new ServerSocket(Integer.parseInt(conf.getListen_port()));
		final ExecutorService tpe = Executors.newCachedThreadPool();
		System.out.println("Proxy listening port:" + conf.getListen_port() + "……");

		while (true) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				socket.setKeepAlive(true);
				// 加入任务列表，等待处理
				tpe.execute(new ProxyTask(socket));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 读取配置文件生成对象
	 */
	private static void readConf(boolean falg, String confName) {
		InputStream in = null;
		Configer conf = Configer.getInstance();
		try {
			if (falg) {
				in = new FileInputStream(confName);
			} else {
				in = SocketProxy.class.getResourceAsStream(confName);
			}
			Properties prop = new Properties();
			prop.load(in);
			conf.setMode(formatString(prop.getProperty("mode")));
			conf.setListen_port(formatString(prop.getProperty("listen_port")));
			conf.setHttps_connect(formatString(prop.getProperty("https_connect")));
			
			conf.setHttp_ip(formatString(prop.getProperty("http_ip")));
			conf.setHttp_port(formatString(prop.getProperty("http_port")));
			conf.setHttp_del(formatString(prop.getProperty("http_del")).split(","));
			conf.setHttp_first(formatString(prop.getProperty("http_first"), true));

			conf.setHttps_ip(formatString(prop.getProperty("https_ip")));
			conf.setHttps_port(formatString(prop.getProperty("https_port")));
			conf.setHttps_del(formatString(prop.getProperty("https_del")).split(","));
			conf.setHttps_first(formatString(prop.getProperty("https_first"), true));
			
			conf.setDns_tcp(formatString(prop.getProperty("dns_tcp")));
			conf.setDns_listen_port(formatString(prop.getProperty("dns_listen_port")));
			conf.setDns_url(formatString(prop.getProperty("dns_url")));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static String formatString(String str) {
		String regex = "^\".*";
		Pattern patter = Pattern.compile(regex);
		Matcher matcher = patter.matcher(str);
		if (matcher.find()) {
			str = str.substring(1, str.length() - 2);
		} else {
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}
	
	private static String formatString(String str, boolean flag) {
		return str.substring(1, str.length() - 4);
	}

}