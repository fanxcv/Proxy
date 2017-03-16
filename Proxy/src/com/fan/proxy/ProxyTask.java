package com.fan.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 将客户端发送过来的数据转发给请求的服务器端，并将服务器返回的数据转发给客户端
 *
 */
public class ProxyTask implements Runnable {
	private Socket socketIn;
	private Socket socketOut;

	private long totalUpload = 0l;// 总计上行比特数
	private long totalDownload = 0l;// 总计下行比特数
	
	private Configer conf = Configer.getInstance();

	public ProxyTask(Socket socket) {
		this.socketIn = socket;
	}

	/** 已连接到请求的服务器 */
	private static final String AUTHORED = "HTTP/1.1 200 Connection established\r\n\r\n";
	/** 本代理登陆失败(此应用暂时不涉及登陆操作) */
	// private static final String UNAUTHORED="HTTP/1.1 407 Unauthorized\r\n\r\n";
	/** 内部错误 */
	private static final String SERVERERROR = "HTTP/1.1 500 Connection FAILED\r\n\r\n";

	@Override
	public void run() {

		try {
			InputStream isIn = socketIn.getInputStream();
			OutputStream osIn = socketIn.getOutputStream();
			// 从客户端流数据中读取头部，获得请求主机和端口
			HttpHeader header = HttpHeader.readHeader(isIn);
			// 如果没解析出请求请求地址和端口，则返回错误信息
			if (header.getHost() == null || header.getPort() == null) {
				osIn.write(SERVERERROR.getBytes());
				osIn.flush();
				return;
			}
			// 查找主机和端口
			if (HttpHeader.METHOD_CONNECT.equalsIgnoreCase(header.getMethod())) {
				socketOut = new Socket(conf.getHttps_ip(), Integer.parseInt(conf.getHttps_port()));
			} else {
				if ("net".equalsIgnoreCase(conf.getMode())) {
					socketOut = new Socket(header.getHost(), Integer.parseInt(header.getPort()));
				} else {
					socketOut = new Socket(conf.getHttp_ip(), Integer.parseInt(conf.getHttp_port()));
				}
			}
			socketOut.setKeepAlive(true);
			InputStream isOut = socketOut.getInputStream();
			OutputStream osOut = socketOut.getOutputStream();
			// 新开一个线程将返回的数据转发给客户端,串行会出问题，尚没搞明白原因
			Thread ot = new DownloadData(isOut, osIn);
			ot.start();
			if (header.getMethod().equals(HttpHeader.METHOD_CONNECT)) {
				// 将已联通信号返回给请求页面
				osIn.write(AUTHORED.getBytes());
				osIn.flush();
			}
			// 转发头消息
			byte[] headerData = header.toString().getBytes();
			totalUpload += headerData.length;
			logRequestMsg("OutData: " + totalUpload);
			osOut.write(headerData);
			osOut.flush();
			// 等待向客户端转发的线程结束
			ot.join();
		} catch (Exception e) {
			e.printStackTrace();
			if (!socketIn.isOutputShutdown()) {
				// 如果还可以返回错误状态的话，返回内部错误
				try {
					socketIn.getOutputStream().write(SERVERERROR.getBytes());
				} catch (IOException e1) {}
			}
		} finally {
			try {
				if (socketIn != null) {
					socketIn.close();
				}
			} catch (IOException e) {
			}
			if (socketOut != null) {
				try {
					socketOut.close();
				} catch (IOException e) {
				}
			}
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
	 * 将服务器端返回的数据转发给客户端
	 * 
	 * @param isOut
	 * @param osIn
	 */
	class DownloadData extends Thread {
		private InputStream isOut;
		private OutputStream osIn;

		DownloadData(InputStream isOut, OutputStream osIn) {
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
						osIn.write(buffer, 0, len);
						osIn.flush();
						totalDownload += len;
					}
					if (socketIn.isOutputShutdown() || socketOut.isClosed()) {
						break;
					}
				}
				logRequestMsg("InData: " + totalDownload);
			} catch (Exception e) {
			}
		}
	}

}