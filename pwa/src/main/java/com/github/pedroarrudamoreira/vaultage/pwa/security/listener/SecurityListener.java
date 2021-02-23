package com.github.pedroarrudamoreira.vaultage.pwa.security.listener;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

public class SecurityListener implements ServletRequestListener {
	
	private static final ThreadLocal<String> PROVIDED_TOKENS = new ThreadLocal<>();
	private static final ThreadLocal<String> USE_BASIC = new ThreadLocal<>();
	
	public static String getToken() {
		return PROVIDED_TOKENS.get();
	}
	
	public static String useBasic() {
		return USE_BASIC.get();
	}

	@Override
	public void requestInitialized(ServletRequestEvent sre) {
		ServletRequest rq = sre.getServletRequest();
		PROVIDED_TOKENS.set(rq.getParameter("value"));
		USE_BASIC.set(rq.getParameter("use_basic"));
	}
	
	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		USE_BASIC.remove();
		PROVIDED_TOKENS.remove();
	}
	
}
