package com.yc.tomcat.core; 

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartTomcat {
	public static void main(String[] args) {
		try {
			new StartTomcat().start();   //启动服务器
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	
		
	}
	
	
	@SuppressWarnings("resource")
	public void  start() throws IOException{
		int port = Integer.parseInt(ReadConfig.getInstance().getProperty("port"));  //获取端口
		
		//启动服务器
		ServerSocket ssk = new ServerSocket(port);
		
		System.out.println("服务器启动,端口为:"+port);
		
		// 扫描解析每个部署到服务器下到项目到web.xml文件
		new ParseUrlPattern();
		
		
		// 解析获取对应后缀类型
		new ParseXml();  
		
		
		
		//创建一个线程池 初始大小20
		ExecutorService  serviceThread = Executors.newFixedThreadPool(20);
		
		// 监听客户端的请求
		Socket sk =null;
		while(true) {
			sk = ssk.accept();		
			//交给线程池去处理
			serviceThread.submit(new ServerService(sk));
		}
		
	}


}
