package com.yc.tomcat.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;

import com.yc.web.core.HttpServletRequest;
import com.yc.web.core.HttpServletResponse;
import com.yc.web.core.Servlet;
import com.yc.web.core.ServletRequest;
import com.yc.web.core.ServletResponse;

public class ServerService implements Runnable{
	private Socket sk;
	private InputStream is;
	private OutputStream os;
	
	public ServerService(Socket sk) {
		this.sk = sk;
	}

	@Override
	public void run() {
		
		
		try {
			this.is = sk.getInputStream();
			this.os =sk.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
			return;  //如果出错，则终止运行
		}
		
		//处理请求
		ServletRequest request = new HttpServletRequest(is);
		
		//解析请求
		request.parse();
		
		
		//处理请求
		
		
		//请求的servlet还是静态资源，那如何判断是动态资源呢？ 如果要是映射到动态资源，则肯定会配置到对应项目到web.xml中
		//所以 ，我们必须在服务器启动到时候就自动扫描每个项目下到web.xml文件，解析其中的映射配置
		String url = request.getUrl();
		
		// url= /DayFresh/login?id=123&name=yc
	
		String urlStr = url.substring(1);  //去掉前面的/ -> DayFresh/login?id=123&name=yc
		String projectName = urlStr.substring(0, urlStr.indexOf("/"));    //  DayFresh
		 
		ServletResponse response = new HttpServletResponse("/"+projectName,os);
		
		//是不是动态资源地址
		String clazz = ParseUrlPattern.getClass(url);  //如果能取到处理类，则说明是动态资源
		if(clazz == null || "".equals(clazz)) {   //当成静态资源访问
			response.sendRedirect(url);
			return;
		}
		
		/*
		 * 处理动态资源
		 * 我到规则：所有到动态资源处理代码->servlet代码必须放到当前项目下到bin目录中
		 */
		
		URLClassLoader loader = null; //类加载器
		URL classPath = null;  //要加载到这个类到地址
	
		
		
		try {
		
			classPath = new URL("file",null,TomcatConstants.BASE_PATH +"/"+projectName +"/bin");
			
			//创建一个类加载器，告诉它到这个路径下加载类
			
			loader = new URLClassLoader(new URL[] {classPath});
			
			//通过类加载器，加载我们需要到这个类 -> 是一个我们自己定义到servlet类
			 Class<?> cls = loader.loadClass(clazz);
			
			 Servlet servlet  = (Servlet) cls.newInstance();  //实例化这个类
			 
			 // 将这个请求交给Servlet到service()方法处理
			 servlet.service(request, response);
			 
		} catch (Exception e) {
			send500(e);
			e.printStackTrace();
		}
		
		
	}
	/**
	 * 发送500错误信息
	 * @param e
	 */
	private void send500(Exception e) {
		try {
			String msg = "HTTP/1.1 500 Error\r\n\r\n" +e.getMessage();
			os.write(msg.getBytes());
			os.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
