package com.fan.proxy;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

	public static void readConf(boolean falg, String confName) {
		InputStream in = null;
		Configer conf = Configer.getInstance();
		try {
			if (falg) {
				in = new FileInputStream(confName);
			} else {
				in = Util.class.getResourceAsStream(confName);
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
