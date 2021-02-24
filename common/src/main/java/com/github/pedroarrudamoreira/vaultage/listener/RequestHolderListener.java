package com.github.pedroarrudamoreira.vaultage.listener;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

public class RequestHolderListener implements ServletRequestListener {
	
	
	private static final String ORIGINAL_URL = ServletRequestListener.class.getCanonicalName()
			+ "_originalRequest";
	
	private static final ThreadLocal<ServletRequest> REQUESTS = new ThreadLocal<>();
	
	public static HttpServletRequest getCurrentRequest() {
		return (HttpServletRequest) REQUESTS.get();
	}

	public static String getOriginalUrl() {
		return REQUESTS.get().getAttribute(ORIGINAL_URL).toString();
	}
	
	@Override
	public void requestInitialized(ServletRequestEvent sre) {
		HttpServletRequest request = (HttpServletRequest) sre.getServletRequest();
		request.setAttribute(ORIGINAL_URL, (request.getRequestURI()));
		REQUESTS.set(sre.getServletRequest());
	}
	
	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		REQUESTS.remove();
	}

}
