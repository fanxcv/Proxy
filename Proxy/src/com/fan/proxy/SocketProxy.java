package com.fan.proxy;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * http 代理程序
 * 
 * @author lulaijun
 *
 */
public class SocketProxy extends Utils {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		SocketProxy sk = new SocketProxy();
		//配置文件路径，默认加载同目录下的c。conf文件，-f 配置文件路径：可以加载指定路径的配置文件
		if (args.length == 2 && "-f".equalsIgnoreCase(args[0])) {
			sk.readConf(true, args[1]);
		} else {
			sk.readConf(false, "c.conf");
		}
		ServerSocket serverSocket = new ServerSocket(Integer.parseInt(conf.getListen_port()));
		final ExecutorService tpe = Executors.newCachedThreadPool();
		System.out.println("Proxy Server Start At " + sdf.format(new Date()));
		System.out.println("listening port:" + conf.getListen_port() + "……");
		System.out.println();
		System.out.println();

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
	private void readConf(boolean falg, String confName) {
		InputStream in = null;
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
			conf.setHttp_first(formatString(prop.getProperty("http_first")));

			conf.setHttps_ip(formatString(prop.getProperty("https_ip")));
			conf.setHttps_port(formatString(prop.getProperty("https_port")));
			conf.setHttps_del(formatString(prop.getProperty("https_del")).split(","));
			conf.setHttps_first(formatString(prop.getProperty("https_first")));

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

	private String formatString(String str) {
		String regex = "\"?([^\"]*)\"?;$";
		Pattern patter = Pattern.compile(regex);
		Matcher matcher = patter.matcher(str);
		if (matcher.find()) {
			str = matcher.group(1);
		}
		return str;
	}

}
