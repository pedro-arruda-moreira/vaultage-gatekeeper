package com.github.pedroarrudamoreira.vaultage.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import lombok.Setter;

public abstract class SwitchingFilter implements Filter {
	@Setter
	protected boolean enabled = true;

	@Override
	public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
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
