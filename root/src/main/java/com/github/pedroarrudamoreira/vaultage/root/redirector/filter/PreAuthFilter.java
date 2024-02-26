package com.github.pedroarrudamoreira.vaultage.root.redirector.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;
import com.github.pedroarrudamoreira.vaultage.filter.SwitchingFilter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

public class PreAuthFilter extends SwitchingFilter {

	@Autowired
	@Setter
	private SessionController sessionController;
	
	@Override
	protected void doFilterImpl(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		if("/".equals(sessionController.getOriginalUrl())
			&& req.getParameter("cli") == null) {
			req.getServletContext().getRequestDispatcher("/select-channel.jsp").forward(req, res);
			return;
		}
		chain.doFilter(req, res);
	}

}
