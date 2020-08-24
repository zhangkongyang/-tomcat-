package com.yc.tomcat.core;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ParseUrlPattern {
	private String basePath = TomcatConstants.BASE_PATH; //获取基础路径
	private static Map<String,String> urlPattern = new HashMap<String,String>();  //解析后到映射
	
	public ParseUrlPattern() {
		parse();
	}

	private void parse() {
		//获取服务器中部署到所有项目
		File[] files = new File(basePath).listFiles(); // 获取路径下所有到文件
		if(files == null || files.length<=0) {  //说明服务器中没有项目
			return;
		}
		String projectName = null;
		File childFile = null; 
		
		for(File fl: files) {  //循环所有项目目录
			projectName=fl.getName();
			
			if(!fl.isDirectory()) {
				continue;
			}
			
			//获取这个项目下web.xml文件
			childFile = new File(fl,"web.xml");
			if(!childFile.exists()) {
				continue;
			}
			
			parseXml(projectName,childFile);

		}
		
	}

	private void parseXml(String projectName, File childFile) {
		SAXReader read = new SAXReader();
		Document doc = null;
		
		try {
			doc = read.read(childFile);

			List<Element> mimes = doc.selectNodes("//servlet");
			
			//循环解析
			for(Element el : mimes) {
				urlPattern.put("/"+projectName+el.selectSingleNode("url-pattern").getText().trim(), el.selectSingleNode("servlet-class").getText().trim());
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}  
		
	}
	
	public static String getClass(String url) {
		return urlPattern.getOrDefault(url, null);
	}
	
	public Map<String,String> getUrlPattern(){
		return urlPattern;
	}

}
