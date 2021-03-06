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
import javax.mail.internet.MimeMessage;
import javax.servlet.Filter;
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
import com.github.pedroarrudamoreira.vaultage.root.filter._2fa.util.EmailCollector;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
@Setter
public class TwoFactorAuthFilter implements Filter, InitializingBean, ServletContextAware {
	private static final String ALREADY_VALIDATED_KEY = TwoFactorAuthFilter.class.getName() + ".ALL_OK";
	public static final String LINK_REQUEST_KEY = "__EMAIL_LINK_%%$$";
	private static final String EMAIL_TOKEN_KEY = "email_token";
	private static final String EMAIL_SENT_KEY = "__EMAIL_SENT_%%$$";
	private boolean enabled;
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

	private ServletContext servletContext;
	@Getter(lazy = true, value = AccessLevel.PRIVATE)
	private final RequestDispatcher emailPasswordDispatcher = servletContext.getRequestDispatcher(
			"/2fa/email/password.html");

	@Getter(lazy = true, value = AccessLevel.PRIVATE)
	private final RequestDispatcher checkEmailDispatcher = servletContext.getRequestDispatcher(
			"/2fa/email/check_email.html");

	@Getter(lazy = true, value = AccessLevel.PRIVATE)
	private final RequestDispatcher emailTemplateDispatcher = servletContext.getRequestDispatcher(
			"/2fa/email/email_template.jsp");


	@Override
	public void doFilter(ServletRequest rq, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if(!enabled) {
			chain.doFilter(rq, response);
			return;
		}
		HttpServletRequest request = (HttpServletRequest) rq;
		HttpSession httpSession = request.getSession();
		if(httpSession.getAttribute(ALREADY_VALIDATED_KEY) != null) {
			chain.doFilter(request, response);
			return;
		}
		String receivedToken = request.getParameter(EMAIL_TOKEN_KEY);
		if(TokenManager.isTokenValid(receivedToken) && TokenManager.removeToken(receivedToken)) {
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
				String emailToken = TokenManager.generateNewToken();

				Message message = new MimeMessage(emailSession);
				message.setFrom(new InternetAddress(smtpUsername));

				Address[] toUser = InternetAddress.parse(addressToSend);

				message.setRecipients(Message.RecipientType.TO, toUser);
				message.setSubject("Login Attempt from Vaultage");
				request.setAttribute(LINK_REQUEST_KEY, formatLinkAddress(request, emailToken));

				StringWriter emailContent = extractEmailContent(request, response);
				message.setContent(emailContent.toString(), "text/html;charset=ISO-8859-1");
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
		StringWriter emailContent = new StringWriter();
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
			String emailPass = request.getParameter("email_password");
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
		Session session = Session.getDefaultInstance(emailProperties,
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
			props.put("mail.smtp.host", smtpHost);
			props.put("mail.smtp.socketFactory.port", smtpPort);
			props.put("mail.smtp.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.auth", String.valueOf(useAuth));
			props.put("mail.smtp.port", smtpPort);
			props.put("mail.smtp.starttls.enable", String.valueOf(useStartTls));
			this.emailProperties = props;
		}
	}



}
