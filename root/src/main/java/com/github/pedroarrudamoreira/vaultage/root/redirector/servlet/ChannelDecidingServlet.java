package com.github.pedroarrudamoreira.vaultage.root.redirector.servlet;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.TokenManager;
import com.github.pedroarrudamoreira.vaultage.accesscontrol.TokenType;
import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;

import lombok.Setter;

public class ChannelDecidingServlet extends HttpServlet {

	public static final String TOKEN_KEY = "token";
	
	public static final String DESKTOP_MODE_KEY = "__DESKTOP_MODE__";

	private static final long serialVersionUID = -7310795831867670109L;
	
	@Setter
	private AuthenticationProvider authProvider;

	@Setter
	private TokenManager tokenManager;

	private Pattern mobilePattern;
	
	public void setMobilePattern(String mobilePattern) {
		this.mobilePattern = Pattern.compile(mobilePattern);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		boolean userAgentMatch = isMobileDevice(req);
		if(userAgentMatch) {
			sendToPWA(req, resp, false);
		} else {
			if(useCliForDesktop()) {
				req.getServletContext().getRequestDispatcher("/cli.jsp").forward(req, resp);
			} else {
				sendToPWA(req, resp, true);
			}
		}
	}

	public boolean isMobileDevice(HttpServletRequest req) {
		String userAgent = req.getHeader("User-Agent").toUpperCase();

		return mobilePattern.matcher(userAgent).matches();
	}

	private boolean useCliForDesktop() {
		return authProvider.getCurrentUser().getUseCliForDesktop();
	}

	private void sendToPWA(HttpServletRequest req, HttpServletResponse resp,
			boolean desktopMode) throws IOException, ServletException {
		req.setAttribute(TOKEN_KEY, tokenManager.generateNewToken(TokenType.GLOBAL));
		req.setAttribute(DESKTOP_MODE_KEY, String.valueOf(desktopMode));
		req.getServletContext().getRequestDispatcher("/mobile.jsp").forward(req, resp);
	}

}
