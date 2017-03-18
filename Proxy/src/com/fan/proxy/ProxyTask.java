package com.fan.proxy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将客户端发送过来的数据转发给请求的服务器端，并将服务器返回的数据转发给客户端
 *
 */
public class ProxyTask extends Utils implements Runnable {
	private Socket socketIn;
	private Socket socketOut;

	private String host;
	private String port;
	private String path;
	private String method;
	private String version;

	private StringBuffer header = null;

	private long totalUpload = 0l;// 总计上行比特数
	private long totalDownload = 0l;// 总计下行比特数

	public ProxyTask(Socket socket) {
		this.socketIn = socket;
	}

	@Override
	public void run() {

		StringBuilder builder = new StringBuilder();
		try {
			builder.append("\r\n").append("Request Time  ：" + sdf.format(new Date()));

			InputStream isIn = socketIn.getInputStream();
			OutputStream osIn = socketIn.getOutputStream();
			// 从客户端流数据中读取头部，获得请求主机和端口
			HeaderProcess(isIn);

			// 添加请求日志信息
			builder.append("\r\n").append("Request Header：" + header);
			builder.append("\r\n").append("From    Host  ：" + socketIn.getInetAddress());
			builder.append("\r\n").append("From    Port  ：" + socketIn.getPort());
			builder.append("\r\n").append("Proxy   Method：" + method);
			builder.append("\r\n").append("Request Host  ：" + host);
			builder.append("\r\n").append("Request Port  ：" + port);

			// 如果没解析出请求请求地址和端口，则返回错误信息
			if (host == null || port == null) {
				osIn.write(SERVERERROR.getBytes());
				osIn.flush();
				return;
			}

			// 查找主机和端口
			if (METHOD_CONNECT.equals(method)) {
				socketOut = new Socket(host, Integer.parseInt(port));
			} else {
				if ("net".equalsIgnoreCase(conf.getMode())) {
					socketOut = new Socket(host, Integer.parseInt(port));
				} else {
					socketOut = new Socket(conf.getHttp_ip(), Integer.parseInt(conf.getHttp_port()));
				}
			}
			socketOut.setKeepAlive(true);
			InputStream isOut = socketOut.getInputStream();
			OutputStream osOut = socketOut.getOutputStream();
			Thread ot = new DataSendThread(isOut, osIn);
			ot.start();
			if (method.equals(METHOD_CONNECT)) {
				osIn.write(AUTHORED.getBytes());
				osIn.flush();
			} else {
				byte[] headerData = header.toString().getBytes();
				totalUpload += headerData.length;
				osOut.write(headerData);
				osOut.flush();
			}
			readForwardDate(isIn, osOut);
			ot.join();
		} catch (Exception e) {
			// e.printStackTrace();
			if (!socketIn.isOutputShutdown()) {
				// 如果还可以返回错误状态的话，返回内部错误
				try {
					socketIn.getOutputStream().write(SERVERERROR.getBytes());
				} catch (IOException e1) {
				}
			}
		} finally {
			try {
				if (socketIn != null) {
					socketIn.close();
				}
				if (socketOut != null) {
					socketOut.close();
				}
			} catch (IOException e) {
			}
			// 纪录上下行数据量和最后结束时间并打印
			builder.append("\r\n").append("Up    Bytes  ：" + totalUpload);
			builder.append("\r\n").append("Down  Bytes  ：" + totalDownload);
			builder.append("\r\n").append("Closed Time  ：" + sdf.format(new Date()));
			builder.append("\r\n");
			logRequestMsg(builder.toString());
		}
	}

	/**
	 * 避免多线程竞争把日志打串行了
	 * 
	 * @param msg
	 */
	private synchronized void logRequestMsg(String msg) {
		System.out.println(msg);
	}

	/**
	 * 读取客户端发送过来的数据，发送给服务器端
	 * 
	 * @param isIn
	 * @param osOut
	 */
	private void readForwardDate(InputStream isIn, OutputStream osOut) {
		byte[] buffer = new byte[4096];
		try {
			int len;
			while ((len = isIn.read(buffer)) != -1) {
				if (len > 0) {
					osOut.write(buffer, 0, len);
					osOut.flush();
				}
				totalUpload += len;
				if (socketIn.isClosed() || socketOut.isClosed()) {
					break;
				}
			}
		} catch (Exception e) {
			try {
				socketOut.close();// 尝试关闭远程服务器连接，中断转发线程的读阻塞状态
			} catch (IOException e1) {
			}
		}
	}

	/**
	 * 将服务器端返回的数据转发给客户端
	 * 
	 * @param isOut
	 * @param osIn
	 */
	class DataSendThread extends Thread {
		private InputStream isOut;
		private OutputStream osIn;

		DataSendThread(InputStream isOut, OutputStream osIn) {
			this.isOut = isOut;
			this.osIn = osIn;
		}

		@Override
		public void run() {
			byte[] buffer = new byte[4096];
			try {
				int len;
				while ((len = isOut.read(buffer)) != -1) {
					if (len > 0) {
						// logData(buffer, 0, len);
						osIn.write(buffer, 0, len);
						osIn.flush();
						totalDownload += len;
					}
					if (socketIn.isOutputShutdown() || socketOut.isClosed()) {
						break;
					}
				}
			} catch (Exception e) {
			}
		}
	}

	public void HeaderProcess(InputStream in) throws IOException {
		String line = null;
		header = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		if (!isEmpty(line = br.readLine())) {
			// 如能识别出请求方式则则继续，不能则退出
			addHeaderMethod(line);
			if (!isEmpty(method)) {
				while (!isEmpty(line = br.readLine())) {
					addHeaderString(line);
				}
				buildHeader();
			}
		}
	}

	private void addHeaderMethod(String str) {
		str = str.replaceAll("\r", "");
		if (str.startsWith(METHOD_CONNECT)) {
			method = METHOD_CONNECT;
		} else if (str.startsWith(METHOD_GET)) {
			method = METHOD_GET;
		} else if (str.startsWith(METHOD_POST)) {
			method = METHOD_POST;
		}
		if (!isEmpty(method)) {
			Pattern p = Pattern.compile("([a-zA-Z ]*?://)?([^/]*)(.*) (HTTP/.*)$");
			Matcher m = p.matcher(str);
			if (m.find()) {
				path = m.group(3);
				version = m.group(4);
			}
		}
	}

	private void addHeaderString(String str) {
		str = str.replaceAll("\r", "");
		if (str.startsWith("Host")) {
			String[] hosts = str.split(":");
			host = hosts[1].trim();
			if (method.endsWith(METHOD_CONNECT)) {
				port = hosts.length == 3 ? hosts[2] : "443";
			} else {
				port = hosts.length == 3 ? hosts[2] : "80";
			}
		}
		String head = str.substring(0, str.indexOf(':'));
		if (method.equals(METHOD_CONNECT)) {
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
		header.append(str).append("\r\n");
	}

	private void buildHeader() {
		String first = null;
		String xhost = host;
		if (port != null && !conf.getHttps_first().contains("[P]") && !"80".equals(port) && !"443".equals(port)) {
			xhost = host + ":" + port;
		}
		if (method.equalsIgnoreCase(METHOD_CONNECT)) {
			first = conf.getHttps_first().replaceAll("\\[M\\]", method).replaceAll("\\[V\\]", version)
					.replaceAll("\\[H\\]", xhost).replaceAll("\\[P\\]", port).replaceAll("\\[U\\]", path);
		} else {
			first = conf.getHttp_first().replaceAll("\\[M\\]", method).replaceAll("\\[V\\]", version)
					.replaceAll("\\[H\\]", xhost).replaceAll("\\[P\\]", port).replaceAll("\\[U\\]", path);
		}
		header.insert(0, first).append("\r\n");
	}

}