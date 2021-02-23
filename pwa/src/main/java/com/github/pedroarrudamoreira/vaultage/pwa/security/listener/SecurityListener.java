package com.github.pedroarrudamoreira.vaultage.pwa.security.listener;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

public class SecurityListener implements ServletRequestListener {
	
	private static final ThreadLocal<String> PROVIDED_TOKENS = new ThreadLocal<>();
	
	public static String getToken() {
		return PROVIDED_TOKENS.get();
	}

	@Override
	public void requestInitialized(ServletRequestEvent sre) {
		ServletRequest rq = sre.getServletRequest();
		PROVIDED_TOKENS.set(rq.getParameter("value"));
	}
	
	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		PROVIDED_TOKENS.remove();
	}
	
}
