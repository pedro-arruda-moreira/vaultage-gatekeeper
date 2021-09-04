package com.github.pedroarrudamoreira.vaultage.root.redirector.servlet;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpHost;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;
import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;

import lombok.Setter;

public class ProxyServlet extends org.mitre.dsmiley.httpproxy.ProxyServlet {
	@Setter
	private AuthenticationProvider userProvider;

	/**
	 * 
	 */
	private static final long serialVersionUID = 7818242091223514627L;
	
	@Override
	protected String rewritePathInfoFromRequest(HttpServletRequest servletRequest) {
		return SessionController.getOriginalUrl();
	}
	
	@Override
	protected HttpHost getTargetHost(HttpServletRequest servletRequest) {
		User currentUser = userProvider.getCurrentUser();
		return new HttpHost("localhost", currentUser.getPort());
	}

}
