package com.github.pedroarrudamoreira.vaultage.root.email.service;

import java.util.Properties;
import java.util.concurrent.ExecutorService;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.github.pedroarrudamoreira.vaultage.root.email.util.EasySSLSocketFactory;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;
@CommonsLog
public class EmailService implements InitializingBean, DisposableBean {
	static final String USE_START_TLS_KEY = "mail.smtp.starttls.enable";
	static final String SMTP_PORT_KEY = "mail.smtp.port";
	static final String USE_AUTH_KEY = "mail.smtp.auth";
	static final String SOCKET_FACTORY_KEY = "mail.smtp.ssl.socketFactory";
	static final String SOCKET_FACTORY_PORT_KEY = "mail.smtp.socketFactory.port";
	static final String SMTP_HOST_KEY = "mail.smtp.host";
	static final String EMAIL_CONTENT_TYPE = "text/html;charset=ISO-8859-1";
	static final String MAIL_USER_KEY = "mail.from";
	@Setter
	private boolean useStartTls;
	@Setter
	private boolean useAuth;
	@Setter
	private String smtpUsername;
	private String password;
	@Setter
	private boolean debug;
	@Setter
	private String smtpHost;
	@Setter
	private String smtpPort;
	@Setter
	private String thisServerHost;
	@Setter
	private EasySSLSocketFactory sslContextFactory;
	@Getter @Setter
	private boolean enabled;
	
	private ExecutorService mailer = ObjectFactory.createDaemonExecutorService(1, 1, 10, "vaultage email sender thread");

	private Properties emailProperties;
	
	public boolean isAuthenticationConfigured() {
		return !this.useAuth || this.password != null;
	}
	
	public void setPassword(String password) {
		if(this.password == null) {
			this.password = password;
		}
	}

	
	public void sendEmail(final String addressToSend, final String subject, String emailContent, DataSource attachment)
			throws MessagingException, AddressException {
		Message message = ObjectFactory.buildMimeMessage(configureSession());
		message.setFrom();

		Address[] toUser = InternetAddress.parse(addressToSend);
		message.setRecipients(Message.RecipientType.TO, toUser);
		message.setSubject(subject);
		
		MimeBodyPart bodyPart = new MimeBodyPart();
	    bodyPart.setContent(emailContent, EMAIL_CONTENT_TYPE);
	    
	    MimeMultipart multipart = new MimeMultipart();
	    multipart.addBodyPart(bodyPart);
	    if(attachment != null) {
	        MimeBodyPart attachmentPart = new MimeBodyPart();
	        attachmentPart.setFileName(attachment.getName());
	        attachmentPart.setDataHandler(new DataHandler(attachment));
	        multipart.addBodyPart(attachmentPart);
	    }


		message.setContent(multipart);
		mailer.execute(() -> {
			try {
				Transport.send(message);
			} catch (MessagingException e) {
				log.error("Error while sending email", e);
			}
		});
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		if(enabled) {
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
	private Session configureSession() {
		Session session = null;
		Authenticator authenticator = null;
		if(useAuth) {
			authenticator = new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication()
				{
					return new PasswordAuthentication(smtpUsername, password);
				}
			};
		}
		session = ObjectFactory.buildEmailSession(emailProperties,
				authenticator);

		session.setDebug(debug);
		return session;
	}

	@Override
	public void destroy() throws Exception {
		mailer.shutdown();
	}

}
