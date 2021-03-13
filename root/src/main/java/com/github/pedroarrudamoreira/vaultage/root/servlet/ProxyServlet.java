package com.github.pedroarrudamoreira.vaultage.root.servlet;

import javax.servlet.http.HttpServletRequest;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;

public class ProxyServlet extends org.mitre.dsmiley.httpproxy.ProxyServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7818242091223514627L;
	
	@Override
	protected String rewritePathInfoFromRequest(HttpServletRequest servletRequest) {
		return SessionController.getOriginalUrl();
	}

}
