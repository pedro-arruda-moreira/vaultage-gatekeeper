package com.github.pedroarrudamoreira.vaultage.root.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.cglib.proxy.UndeclaredThrowableException;
import org.springframework.web.context.ServletContextAware;

import com.github.pedroarrudamoreira.vaultage.root.util.ObjectFactory;

import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;

public class MobileServlet extends HttpServlet implements ServletContextAware {

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
	@Setter
	private String useBasic;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String userAgent = req.getHeader("User-Agent").toUpperCase();

		if(mobilePattern.matcher(userAgent).matches()) {
			req.setAttribute("use_basic", useBasic);
			getMobileDispatcher().forward(req, resp);
		} else {
			getVaultageCliDispatcher().forward(req, resp);
		}
	}

	@Override
	public void init() {
		try {
			Properties prop = ObjectFactory.buildProperties();
			@Cleanup InputStream is = MobileServlet.class.getResourceAsStream("mobile.properties");
			prop.load(is);
			mobilePattern = Pattern.compile(prop.getProperty("hints"));
		} catch (Exception e) {
			throw new UndeclaredThrowableException(e);
		}
	}

}
