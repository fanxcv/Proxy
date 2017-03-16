package com.fan.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析头部信息
 *
 */
public final class HttpHeader {

	private List<String> header = new ArrayList<String>();
	private List<String> tmpheader = new ArrayList<String>();

	private String method;
	private String host;
	private String port;
	private String path;
	private String version;

	private Configer conf = Configer.getInstance();

	public static final int MAXLINESIZE = 10240;

	public static final String METHOD_GET = "GET";
	public static final String METHOD_POST = "POST";
	public static final String METHOD_CONNECT = "CONNECT";

	private HttpHeader() {
	}

	/**
	 * 从数据流中读取请求头部信息，必须在放在流开启之后，任何数据读取之前
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static final HttpHeader readHeader(InputStream in) throws IOException {
		HttpHeader header = new HttpHeader();
		StringBuilder sb = new StringBuilder();
		// 先读出交互协议来，
		char c = 0;
		while ((c = (char) in.read()) != '\n') {
			sb.append(c);
			if (sb.length() == MAXLINESIZE) {// 不接受过长的头部字段
				break;
			}
		}
		// 如能识别出请求方式则则继续，不能则退出
		if (header.addHeaderMethod(sb.toString()) != null) {
			do {
				sb = new StringBuilder();
				while ((c = (char) in.read()) != '\n') {
					sb.append(c);
					if (sb.length() == MAXLINESIZE) {// 不接受过长的头部字段
						break;
					}
				}
				if (sb.length() > 1 && header.notTooLong()) {// 如果头部包含信息过多，抛弃剩下的部分
					header.addHeaderString(sb.substring(0, sb.length() - 1));
				} else {
					break;
				}
			} while (true);
			header.buildHeader();
		}
		return header;
	}

	/**
	 * 
	 * @param str
	 */
	private void addHeaderString(String str) {
		str = str.replaceAll("\r", "");
		if (str.startsWith("Host")) {// 解析主机和端口
			String[] hosts = str.split(":");
			host = hosts[1].trim();
			if (method.endsWith(METHOD_CONNECT)) {
				port = hosts.length == 3 ? hosts[2] : "443";// https默认端口为443
			} else if (method.endsWith(METHOD_GET) || method.endsWith(METHOD_POST)) {
				port = hosts.length == 3 ? hosts[2] : "80";// http默认端口为80
			}
		}
		String head = str.substring(0, str.indexOf(':'));
		if (method.equalsIgnoreCase(METHOD_CONNECT)) {
			for (String s : conf.getHttps_del()) {
				if (s.equalsIgnoreCase(head)) {
					return;
				}
			}
		} else {
			for (String s : conf.getHttp_del()) {
				if (s.equalsIgnoreCase(head)) {
					return;
				}
			}
		}
		tmpheader.add(str);
	}

	/**
	 * 判定请求方式
	 * 
	 * @param str
	 * @return
	 */
	private String addHeaderMethod(String str) {
		str = str.replaceAll("\r", "");
		// header.add(str);
		if (str.startsWith(METHOD_CONNECT)) {// https链接请求代理
			method = METHOD_CONNECT;
		} else if (str.startsWith(METHOD_GET)) {// http GET请求
			method = METHOD_GET;
		} else if (str.startsWith(METHOD_POST)) {// http POST请求
			method = METHOD_POST;
		}
		Pattern p = Pattern.compile("([a-zA-Z ]*?://)?([^/]*)(.*) (HTTP/.*)$");
		Matcher m = p.matcher(str);
		if (m.find()) {
			path = m.group(3);
			version = m.group(4);
		}
		return method;
	}

	private void buildHeader() {
		String first = null;
		String xhost = host;
		if (port != null && !"80".equalsIgnoreCase(port) && !"443".equalsIgnoreCase(port)) {
			xhost = host + ":" + port;
		}
		if (method.equalsIgnoreCase(METHOD_CONNECT)) {
			first = conf.getHttps_first().replaceAll("\\[M\\]", method).replaceAll("\\[V\\]", version).replaceAll("\\[H\\]", xhost)
					.replaceAll("\\[U\\]", path);
		} else {
			first = conf.getHttp_first().replaceAll("\\[M\\]", method).replaceAll("\\[V\\]", version).replaceAll("\\[H\\]", xhost)
					.replaceAll("\\[U\\]", path);
		}
		header.add(first);
		header.addAll(tmpheader);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String str : header) {
			sb.append(str).append("\r\n");
		}
		sb.append("\r\n");
		return sb.toString();
	}

	public boolean notTooLong() {
		return header.size() <= 32;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
