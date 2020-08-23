package com.yc.web.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import com.yc.tomcat.core.ParseXml;
import com.yc.tomcat.core.ReadConfig;
import com.yc.tomcat.core.TomcatConstants;

/**
 * 基于HTTP协议的响应
 * 
 * @author 张孔洋
 * @data Aug 22, 2020
 */
public class HttpServletResponse implements ServletResponse {
	private OutputStream os;
	private String basePath = TomcatConstants.BASE_PATH;
	private String projectName;
	
	
	
	public HttpServletResponse(String projectName,OutputStream os) {
		this.os = os;
		this.projectName=projectName;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		String msg = "HTTP/1.1 200 OK\r\nContent-Type:text/html;charset=utf-8\r\n\r\n";
		os.write(msg.getBytes());
		os.flush();
		return new PrintWriter(os);
	}

	@Override
	public void sendRedirect(String url) {
		if (url == null || "".equals(url)) {
			error404(url);
			return;
		}

		if (url.indexOf("/") == url.lastIndexOf("/") && url.indexOf("/") < url.length()) { // DayFresh, 即只指定了项目名
			send302(url);
		} else {
			if (url.endsWith("/")) { // 说明没有指定具体的资源 /Shop/ /Shop/back/

				String defaultPath = ReadConfig.getInstance().getProperty("default");
				// 读取默认资源
				File fl = new File(basePath, url.substring(1) + defaultPath);
				if (!fl.exists()) {
					error404(url);
					return;
				}

				// 存在
				send200(readFile(fl), defaultPath.substring(defaultPath.lastIndexOf(".") + 1).toLowerCase());
			} else {
				File fl = new File(basePath, url.substring(1));
				if (!fl.exists() || !fl.isFile()) {
					error404(url);
					return;
				}
				send200(readFile(fl), url.substring(url.lastIndexOf(".") + 1).toLowerCase());
			}
		}
	}

	private void send302(String url) {
		try {
			String msg = "HTTP/1.1 302 Moved Temporarily\r\nContent-Type:text/html;charset=utf-8\r\nLocation:" + url
					+ "/\r\n\r\n";
			os.write(msg.getBytes());
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private byte[] readFile(File f1) {
		try (FileInputStream fis = new FileInputStream(f1)) {
			byte[] bt = new byte[fis.available()];
			fis.read(bt);
			return bt;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void send200(byte[] bt, String extension) {
		try {
			String contentType = ParseXml.getContentType(extension); // 根据后缀查类型
			String msg = "HTTP/1.1 200 OK\r\nContent-Type:" + contentType + "\r\nContent-Length:" + bt.length
					+ "\r\n\r\n";
			os.write(msg.getBytes());
			os.write(bt);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * 报错404
	 * 
	 * @param url
	 */
	private void error404(String url) {
		try {
			String errInfo = "<h1>HTTP Status 404" + url + "</h1>";
			String msg = "HTTP/1.1 404 File Not Found\r\nContent-Type:text/html;charset=utf-t\r\nContent-Length:"
					+ errInfo.length() + "\r\n\r\n" + errInfo;
			os.write(msg.getBytes());
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
