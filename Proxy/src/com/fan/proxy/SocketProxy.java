package com.fan.proxy;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * http 代理程序
 * 
 * @author lulaijun
 *
 */
public class SocketProxy {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		if (args.length == 2 && "-f".equalsIgnoreCase(args[0])) {
			Util.readConf(true, args[1]);
		} else {
			Util.readConf(true, "c.conf");
		}
		Configer conf = Configer.getInstance();
		//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		ServerSocket serverSocket = new ServerSocket(Integer.parseInt(conf.getListen_port()));
		final ExecutorService tpe = Executors.newCachedThreadPool();
		//System.out.println("Proxy Server Start At " + sdf.format(new Date()));
		//System.out.println("listening port:" + conf.getListen_port() + "……");

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

}