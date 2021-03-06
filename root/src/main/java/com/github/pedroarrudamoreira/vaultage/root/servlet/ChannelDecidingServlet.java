package com.github.pedroarrudamoreira.vaultage.root.servlet;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.ServletContextAware;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class ChannelDecidingServlet extends HttpServlet implements ServletContextAware {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7310795831867670109L;
	
	@Setter
	private ServletContext servletContext;
	
	@Getter(lazy = true, value = AccessLevel.PRIVATE)
	private final RequestDispatcher vaultageCliDispatcher = servletContext.getRequestDispatcher(
			"/dist/vaultage");
	@Getter(lazy = true, value = AccessLevel.PRIVATE)
	private final RequestDispatcher mobileDispatcher = servletContext.getRequestDispatcher(
			"/mobile.jsp");

	private Pattern mobilePattern;
	
	public void setMobilePattern(String mobilePattern) {
		this.mobilePattern = Pattern.compile(mobilePattern);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String userAgent = req.getHeader("User-Agent").toUpperCase();

		if(mobilePattern.matcher(userAgent).matches()) {
			req.setAttribute("use_basic", "true");
			getMobileDispatcher().forward(req, resp);
		} else {
			getVaultageCliDispatcher().forward(req, resp);
		}
	}

}
