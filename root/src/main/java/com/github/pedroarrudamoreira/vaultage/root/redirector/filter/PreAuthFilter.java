package com.github.pedroarrudamoreira.vaultage.root.redirector.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;

public class PreAuthFilter extends OncePerRequestFilter {
	
	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		if("/".equals(SessionController.getOriginalUrl())
			&& req.getParameter("cli") == null) {
			res.sendRedirect("/select-channel");
			return;
		}
		chain.doFilter(req, res);
	}

}
