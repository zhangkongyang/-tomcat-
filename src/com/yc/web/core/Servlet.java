package com.yc.web.core;

/**
 * Servlet接口定义
 * @author 张孔洋
 * @data Aug 21, 2020
 */
public interface Servlet {
	public void init();
	
	public void service(ServletRequest request, ServletResponse response);
	
	public void doGet(ServletRequest request, ServletResponse response);
	
	public void doPost(ServletRequest request, ServletResponse response);

	
	


}
