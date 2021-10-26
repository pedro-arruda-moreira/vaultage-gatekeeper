package com.github.pedroarrudamoreira.vaultage.root.redirector.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;
import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;

import lombok.Setter;

public class PreAuthFilter extends HttpFilter {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Setter
	private AuthenticationProvider authProvider;
	
	@Override
	protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		if("/".equals(SessionController.getOriginalUrl())
			&& req.getParameter("cli") == null) {
			res.sendRedirect("/select-channel");
			return;
		}
		chain.doFilter(req, res);
	}

}
