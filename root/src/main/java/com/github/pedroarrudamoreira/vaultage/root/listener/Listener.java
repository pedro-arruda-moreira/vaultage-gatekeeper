package com.github.pedroarrudamoreira.vaultage.root.listener;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

public class Listener implements ServletRequestListener {

	private static final String ORIGINAL_URL = Listener.class.getCanonicalName()
			+ "_originalRequest";

	public static String getOriginalUrl(ServletRequest rq) {
		return rq.getAttribute(ORIGINAL_URL).toString();
	}

	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		return;
	}

	@Override
	public void requestInitialized(ServletRequestEvent sre) {
		ServletRequest request = sre.getServletRequest();
		if(request instanceof HttpServletRequest) {
			request.setAttribute(ORIGINAL_URL, ((HttpServletRequest)request).getRequestURI());
		}
	}

}
