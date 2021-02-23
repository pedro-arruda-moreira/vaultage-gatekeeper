package com.github.pedroarrudamoreira.vaultage.root.servlet;

import javax.servlet.http.HttpServletRequest;

import com.github.pedroarrudamoreira.vaultage.root.listener.Listener;

public class ProxyServlet extends org.mitre.dsmiley.httpproxy.ProxyServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7818242091223514627L;
	
	@Override
	protected String rewritePathInfoFromRequest(HttpServletRequest servletRequest) {
		return Listener.getOriginalUrl(servletRequest);
	}

}