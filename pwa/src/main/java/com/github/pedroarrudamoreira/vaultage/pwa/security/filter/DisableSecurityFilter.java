package com.github.pedroarrudamoreira.vaultage.pwa.security.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;
import com.github.pedroarrudamoreira.vaultage.filter.SwitchingFilter;

import lombok.extern.apachecommons.CommonsLog;
@CommonsLog
public class DisableSecurityFilter extends SwitchingFilter {

	@Override
	protected void doFilterImpl(HttpServletRequest request, HttpServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		log.warn("PWA security is disabled!");
		request.getRequestDispatcher(SessionController.getOriginalUrl().replace(
				"/pwa/", "/")).forward(request, response);
	}

}
