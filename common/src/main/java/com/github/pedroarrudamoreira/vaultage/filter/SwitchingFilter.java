package com.github.pedroarrudamoreira.vaultage.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import lombok.Setter;

public abstract class SwitchingFilter extends OncePerRequestFilter {
	@Setter
	protected boolean enabled = true;

	@Override
	protected final void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if(!enabled) {
			chain.doFilter(request, response);
			return;
		}
		doFilterImpl(request, response, chain);
	}

	protected abstract void doFilterImpl(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException;

}
