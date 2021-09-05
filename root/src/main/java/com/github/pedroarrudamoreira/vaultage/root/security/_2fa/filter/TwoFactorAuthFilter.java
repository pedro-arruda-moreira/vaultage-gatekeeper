package com.github.pedroarrudamoreira.vaultage.root.security._2fa.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.mail.MessagingException;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.ServletContextAware;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.TokenManager;
import com.github.pedroarrudamoreira.vaultage.accesscontrol.TokenType;
import com.github.pedroarrudamoreira.vaultage.filter.SwitchingFilter;
import com.github.pedroarrudamoreira.vaultage.root.email.service.EmailService;
import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
@Setter
public class TwoFactorAuthFilter extends SwitchingFilter implements ServletContextAware {
	static final String EMAIL_CONTENT_TYPE = "text/html;charset=ISO-8859-1";
	static final String EMAIL_PASSWORD_REQUEST_KEY = "email_password";
	static final String ALREADY_VALIDATED_KEY = TwoFactorAuthFilter.class.getName() + ".ALL_OK";
	static final String CHECK_EMAIL_HTML_LOCATION = "/2fa/email/check_email.html";
	static final String PASSWORD_HTML_LOCATION = "/2fa/email/password.html";
	static final String EMAIL_TEMPLATE_JSP_LOCATION = "/2fa/email/email_template.jsp";
	static final String SUBJECT = "Login Attempt from Vaultage";

	public static final String EMAIL_TOKEN_KEY = "email_token";
	static final String EMAIL_SENT_KEY = "__EMAIL_SENT_%%$$";
	
	private EmailService emailService;
	
	private AuthenticationProvider authProvider;
	
	private String thisServerHost;

	private ServletContext servletContext;
	@Getter(lazy = true, value = AccessLevel.PRIVATE)
	private final RequestDispatcher emailPasswordDispatcher = servletContext.getRequestDispatcher(
			PASSWORD_HTML_LOCATION);

	@Getter(lazy = true, value = AccessLevel.PRIVATE)
	private final RequestDispatcher checkEmailDispatcher = servletContext.getRequestDispatcher(
			CHECK_EMAIL_HTML_LOCATION);


	@Override
	protected void doFilterImpl(ServletRequest rq, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if(!emailService.isEnabled()) {
			throw new IllegalStateException("cannot send emails because e-mail service"
					+ " is not enabled. Please enable it and try again.");
		}
		HttpServletRequest request = (HttpServletRequest) rq;
		HttpSession httpSession = request.getSession();
		if(httpSession.getAttribute(ALREADY_VALIDATED_KEY) != null) {
			chain.doFilter(request, response);
			return;
		}
		String receivedToken = request.getParameter(EMAIL_TOKEN_KEY);
		if(TokenManager.isTokenValid(receivedToken, TokenType.SESSION) &&
				TokenManager.removeToken(receivedToken)) {
			httpSession.setAttribute(ALREADY_VALIDATED_KEY, ObjectFactory.PRESENT);
			chain.doFilter(request, response);
			return;
		}
		if(!validateEmailPassword(response, request)) {
			return;
		}


		doSendMail(request, response, httpSession);

	}

	private void doSendMail(HttpServletRequest request, ServletResponse response,
			HttpSession httpSession)
					throws IOException, ServletException {

		try {
			synchronized (request.getSession()) {
				if(httpSession.getAttribute(EMAIL_SENT_KEY) == null) {
					String token = TokenManager.generateNewToken(TokenType.SESSION);
					String emailContent = extractEmailContent(token, request);

					emailService.sendEmail(authProvider.getCurrentUser().getEmail(), SUBJECT, emailContent, null);
					httpSession.setAttribute(EMAIL_SENT_KEY, ObjectFactory.PRESENT);

				}
			}
			getCheckEmailDispatcher().forward(request, response);

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	private String extractEmailContent(String token, HttpServletRequest request)
			throws ServletException, IOException {
		@Cleanup InputStream htmlStream = TwoFactorAuthFilter.class.getResourceAsStream("email_template.html");
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(htmlStream));
		String line = null;
		StringBuilder bld = new StringBuilder();
		while((line = bufferedReader.readLine()) != null) {
			bld.append(line).append('\n');
		}
		return String.format(bld.toString(), formatFormAction(request), token);
	}

	private String formatFormAction(HttpServletRequest request) {
		return String.format("http%s://%s",
				(request.isSecure() ? "s" : StringUtils.EMPTY),
				thisServerHost);
	}

	private boolean validateEmailPassword(ServletResponse response, HttpServletRequest request)
			throws ServletException, IOException {
		if(!emailService.isAuthenticationConfigured()) {
			String emailPass = request.getParameter(EMAIL_PASSWORD_REQUEST_KEY);
			if(emailPass == null) {
				getEmailPasswordDispatcher().forward(request, response);
				return false;
			} else {
				emailService.setPassword(emailPass);
			}
		}
		return true;
	}
}
