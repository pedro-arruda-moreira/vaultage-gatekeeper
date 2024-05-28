package com.github.pedroarrudamoreira.vaultage.root.redirector.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;
import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import com.github.pedroarrudamoreira.vaultage.root.vault.sync.VaultSynchronizer;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

public class ProxyServlet extends org.mitre.dsmiley.httpproxy.ProxyServlet {
	@Setter
	private AuthenticationProvider authProvider;
	
	@Setter
	private VaultSynchronizer vaultSynchronizer;

	@Autowired
	@Setter
	private SessionController sessionController;

	/**
	 * 
	 */
	private static final long serialVersionUID = 7818242091223514627L;
	
	@Override
	protected String rewritePathInfoFromRequest(HttpServletRequest servletRequest) {
		return sessionController.getOriginalUrl();
	}
	
	@Override
	protected HttpHost getTargetHost(HttpServletRequest servletRequest) {
		User currentUser = authProvider.getCurrentUser();
		return new HttpHost("localhost", currentUser.getPort());
	}
	
	@Override
	protected HttpResponse doExecute(HttpServletRequest servletRequest, HttpServletResponse servletResponse,
			HttpRequest proxyRequest) throws IOException {
		return vaultSynchronizer.runSync(
				() -> super.doExecute(servletRequest, servletResponse, proxyRequest));
	}

}
