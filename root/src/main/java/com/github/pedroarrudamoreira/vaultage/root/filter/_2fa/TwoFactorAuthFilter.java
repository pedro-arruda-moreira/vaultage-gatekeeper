package com.github.pedroarrudamoreira.vaultage.root.filter._2fa;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.context.ServletContextAware;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.TokenManager;
import com.github.pedroarrudamoreira.vaultage.accesscontrol.TokenType;
import com.github.pedroarrudamoreira.vaultage.filter.SwitchingFilter;
import com.github.pedroarrudamoreira.vaultage.root.filter._2fa.ssl.EasySSLSocketFactory;
import com.github.pedroarrudamoreira.vaultage.root.filter._2fa.util.EmailCollector;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
@Setter
public class TwoFactorAuthFilter extends SwitchingFilter implements InitializingBean, ServletContextAware {
	static final String EMAIL_CONTENT_TYPE = "text/html;charset=ISO-8859-1";
	static final String MAIL_USER_KEY = "mail.from";
	static final String EMAIL_PASSWORD_REQUEST_KEY = "email_password";
	static final String USE_START_TLS_KEY = "mail.smtp.starttls.enable";
	static final String SMTP_PORT_KEY = "mail.smtp.port";
	static final String USE_AUTH_KEY = "mail.smtp.auth";
	static final String SOCKET_FACTORY_KEY = "mail.smtp.ssl.socketFactory";
	static final String SOCKET_FACTORY_PORT_KEY = "mail.smtp.socketFactory.port";
	static final String SMTP_HOST_KEY = "mail.smtp.host";
	static final String ALREADY_VALIDATED_KEY = TwoFactorAuthFilter.class.getName() + ".ALL_OK";
	static final String CHECK_EMAIL_HTML_LOCATION = "/2fa/email/check_email.html";
	static final String PASSWORD_HTML_LOCATION = "/2fa/email/password.html";
	static final String EMAIL_TEMPLATE_JSP_LOCATION = "/2fa/email/email_template.jsp";
	public static final String LINK_REQUEST_KEY = "__EMAIL_LINK_%%$$";
	static final String EMAIL_TOKEN_KEY = "email_token";
	static final String EMAIL_SENT_KEY = "__EMAIL_SENT_%%$$";
	private boolean useAuth;
	private boolean debug;
	private boolean useStartTls;

	private String smtpHost;

	private String smtpPort;

	private String smtpUsername;

	private String addressToSend;

	private String password;

	private String thisServerHost;

	private Properties emailProperties;
	
	private EasySSLSocketFactory sslContextFactory;

	private ServletContext servletContext;
	@Getter(lazy = true, value = AccessLevel.PRIVATE)
	private final RequestDispatcher emailPasswordDispatcher = servletContext.getRequestDispatcher(
			PASSWORD_HTML_LOCATION);

	@Getter(lazy = true, value = AccessLevel.PRIVATE)
	private final RequestDispatcher checkEmailDispatcher = servletContext.getRequestDispatcher(
			CHECK_EMAIL_HTML_LOCATION);

	@Getter(lazy = true, value = AccessLevel.PRIVATE)
	private final RequestDispatcher emailTemplateDispatcher = servletContext.getRequestDispatcher(
			EMAIL_TEMPLATE_JSP_LOCATION);


	@Override
	protected void doFilterImpl(ServletRequest rq, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) rq;
		HttpSession httpSession = request.getSession();
		if(httpSession.getAttribute(ALREADY_VALIDATED_KEY) != null) {
			chain.doFilter(request, response);
			return;
		}
		String receivedToken = request.getParameter(EMAIL_TOKEN_KEY);
		if(TokenManager.isTokenValid(receivedToken, TokenType.SESSION) &&
				TokenManager.removeToken(receivedToken)) {
			httpSession.setAttribute(ALREADY_VALIDATED_KEY, this);
			chain.doFilter(request, response);
			return;
		}
		if(!validateEmailPassword(response, request)) {
			return;
		}
		Session emailSession = configureSession();


		doSendMail(request, response, emailSession, httpSession);

	}

	private void doSendMail(HttpServletRequest request, ServletResponse response,
			Session emailSession, HttpSession httpSession)
					throws IOException, ServletException {

		try {
			if(httpSession.getAttribute(EMAIL_SENT_KEY) == null) {
				String emailToken = TokenManager.generateNewToken(TokenType.SESSION);

				Message message = ObjectFactory.buildMimeMessage(emailSession);
				message.setFrom();

				Address[] toUser = InternetAddress.parse(addressToSend);

				message.setRecipients(Message.RecipientType.TO, toUser);
				message.setSubject("Login Attempt from Vaultage");
				request.setAttribute(LINK_REQUEST_KEY, formatLinkAddress(request, emailToken));

				StringWriter emailContent = extractEmailContent(request, response);
				message.setContent(emailContent.toString(), EMAIL_CONTENT_TYPE);
				Transport.send(message);
				httpSession.setAttribute(EMAIL_SENT_KEY, this);

			}
			getCheckEmailDispatcher().forward(request, response);

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	private StringWriter extractEmailContent(HttpServletRequest request, ServletResponse response)
			throws ServletException, IOException {
		StringWriter emailContent = ObjectFactory.buildStringWriter();
		PrintWriter wr = new PrintWriter(emailContent);

		getEmailTemplateDispatcher().include(request, new EmailCollector(response, wr));
		return emailContent;
	}

	private String formatLinkAddress(HttpServletRequest request, String emailToken) {
		return String.format("http%s://%s/?%s=%s",
				(request.isSecure() ? "s" : StringUtils.EMPTY),
				thisServerHost,
				EMAIL_TOKEN_KEY,
				emailToken);
	}

	private boolean validateEmailPassword(ServletResponse response, HttpServletRequest request)
			throws ServletException, IOException {
		if(useAuth && password == null) {
			String emailPass = request.getParameter(EMAIL_PASSWORD_REQUEST_KEY);
			if(emailPass == null) {
				getEmailPasswordDispatcher().forward(request, response);
				return false;
			} else {
				password = emailPass;
			}
		}
		return true;
	}

	private Session configureSession() {
		Authenticator authenticator = null;
		if(useAuth) {
			authenticator = new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication()
				{
					return new PasswordAuthentication(smtpUsername, password);
				}
			};
		}
		Session session = ObjectFactory.buildEmailSession(emailProperties,
				authenticator);

		session.setDebug(debug);
		return session;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if(enabled) {
			Assert.notNull(addressToSend, "addressToSend required.");
			Assert.notNull(smtpHost, "smtpHost required.");
			Assert.notNull(smtpPort, "smtpPort required.");
			Assert.notNull(smtpUsername, "smtpUsername required.");
			Assert.notNull(thisServerHost, "thisServerHost required.");
			Properties props = ObjectFactory.buildProperties();
			props.setProperty(SMTP_HOST_KEY, smtpHost);
			props.setProperty(SMTP_PORT_KEY, smtpPort);
			props.setProperty(SOCKET_FACTORY_PORT_KEY, smtpPort);
			props.put(SOCKET_FACTORY_KEY, sslContextFactory);
			props.setProperty(USE_AUTH_KEY, String.valueOf(useAuth));
			props.setProperty(USE_START_TLS_KEY, String.valueOf(useStartTls));
			props.setProperty(MAIL_USER_KEY, smtpUsername);
			this.emailProperties = props;
		}
	}



}
