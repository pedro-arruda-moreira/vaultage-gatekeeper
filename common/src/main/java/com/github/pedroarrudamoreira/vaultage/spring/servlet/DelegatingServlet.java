package com.github.pedroarrudamoreira.vaultage.spring.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import org.springframework.web.context.ContextLoaderListener;

public class DelegatingServlet extends HttpServlet {
	
	private static final long serialVersionUID = 7070786119124190084L;
	private Servlet targetServlet; 

	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		targetServlet.service(req, res);
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String beanName = config.getServletName();
		targetServlet = (Servlet) ContextLoaderListener.getCurrentWebApplicationContext().getBean(
				beanName);
		targetServlet.init(config);
	}
	
	@Override
	public void destroy() {
		super.destroy();
		targetServlet.destroy();
	}
	
}
