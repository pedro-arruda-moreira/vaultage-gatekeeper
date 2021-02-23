package com.github.pedroarrudamoreira.vaultage.pwa.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.TokenManager;

import lombok.Getter;

public class SecurityFilter implements Filter {
	
	private static final String TOKEN_KEY = "security_token";

	private ServletContext context;
	
	@Getter(lazy = true)
	private final RequestDispatcher blockedDispatcher = context.getRequestDispatcher("/blocked.html");

	@Override
	public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain fc)
			throws IOException, ServletException {
		String token = rq.getParameter("token");
		HttpSession session = ((HttpServletRequest)rq).getSession();
		if(StringUtils.isBlank(token)) {
			token = (String)session.getAttribute(TOKEN_KEY);
		}
		if(StringUtils.isNotBlank(token) && TokenManager.isTokenValid(token)) {
			session.setAttribute(TOKEN_KEY, token);
			fc.doFilter(rq, rs);
			return;
		}
		getBlockedDispatcher().forward(rq, rs);
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		context = filterConfig.getServletContext();
	}

}
