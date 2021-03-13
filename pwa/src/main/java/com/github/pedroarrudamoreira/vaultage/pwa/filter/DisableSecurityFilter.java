package com.github.pedroarrudamoreira.vaultage.pwa.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;
import com.github.pedroarrudamoreira.vaultage.filter.SwitchingFilter;

public class DisableSecurityFilter extends SwitchingFilter {

	@Override
	protected void doFilterImpl(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		request.getServletContext().log("CAUTION: PWA security is disabled!");
		request.getRequestDispatcher(SessionController.getOriginalUrl().replace(
				"/pwa/", "/")).forward(request, response);
	}

}
