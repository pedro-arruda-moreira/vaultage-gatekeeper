package com.github.pedroarrudamoreira.vaultage.root.email.service;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.AuthenticatorAccessor;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.github.pedroarrudamoreira.vaultage.root.email.util.EasySSLSocketFactory;
import com.github.pedroarrudamoreira.vaultage.test.util.TestUtils;
import com.github.pedroarrudamoreira.vaultage.test.util.mockito.ArgumentCatcher;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;
@RunWith(PowerMockRunner.class)
@PrepareForTest({ObjectFactory.class, Session.class, Transport.class})
public class EmailServiceTest {
	private static final String FAKE_EMAIL_CONTENT = "hello!";
	private static final String FAKE_SUBJECT = "Greetings";
	private static final String STRING_FALSE = "false";
	private static final String STRING_TRUE = "true";
	private static final String FAKE_SMTP_PORT = "1234";
	private static final String FAKE_PASSWORD = "my_p4ssw0rd";
	private static final String FAKE_SMTP_HOST = "mail.test.com";
	private static final String FAKE_EMAIL_ADDRESS = "test@test.com";
	private static final String FAKE_FILE_NAME = "document.pdf";
	
	private EmailService impl;
	
	private Authenticator obtainedAuthenticator;

	private Properties properties;
	
	@Mock
	private Session emailSessionMock;
	
	@Mock
	private MimeMessage mimeMessageMock;
	
	@Mock
	private EasySSLSocketFactory mockSslFactory;
	
	@Mock
	private DataSource mockDataSource;
	
	@Mock
	private ExecutorService mockExecutorService;
	
	@BeforeClass
	public static void setupStatic() {
		TestUtils.doPrepareForTest();
	}
	
	@Before
	public void setup() {
		setupStatic();
		properties = new Properties();
		PowerMockito.when(ObjectFactory.buildMimeMessage(emailSessionMock)).thenReturn(mimeMessageMock);
		PowerMockito.when(ObjectFactory.buildProperties()).thenReturn(properties);
		PowerMockito.when(ObjectFactory.buildEmailSession(Mockito.any(), Mockito.any())).then(
				new ArgumentCatcher<Session>(emailSessionMock,
						v -> obtainedAuthenticator = v.get(), 1));
		PowerMockito.when(ObjectFactory.buildDaemonExecutorService(Mockito.eq(1),
				Mockito.eq(1), Mockito.eq(10), Mockito.any())).thenReturn(mockExecutorService);
		Mockito.doAnswer((i) -> {
			i.getArgument(0, Runnable.class).run();
			return null;
		}).when(mockExecutorService).execute(Mockito.any());
		impl = new EmailService();
		impl.setSslContextFactory(mockSslFactory);
		impl.setSmtpUsername(FAKE_EMAIL_ADDRESS);
	}
	
	
	@Test
	public void testAfterPropertiesSet_NotEnabled() throws Exception {
		impl.afterPropertiesSet();
		PowerMockito.verifyStatic(ObjectFactory.class, Mockito.never());
		ObjectFactory.buildProperties();
		Assert.assertEquals(0, properties.size());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAfterPropertiesSet_RequiredFieldNotSet() throws Exception {
		impl.setEnabled(true);
		impl.afterPropertiesSet();
	}

	@Test
	public void testAfterPropertiesSet_OK() throws Exception {
		impl.setEnabled(true);
		impl.setDebug(false);
		impl.setPassword(FAKE_PASSWORD);
		impl.setSmtpHost(FAKE_SMTP_HOST);
		impl.setSmtpPort(FAKE_SMTP_PORT);
		impl.setThisServerHost("localhost");
		impl.setUseAuth(true);
		impl.setUseStartTls(false);
		impl.afterPropertiesSet();
		Assert.assertEquals(7, properties.size());
		Assert.assertEquals(STRING_FALSE, properties.get(EmailService.USE_START_TLS_KEY));
		Assert.assertEquals(STRING_TRUE, properties.get(EmailService.USE_AUTH_KEY));
		Assert.assertEquals(FAKE_SMTP_HOST, properties.get(EmailService.SMTP_HOST_KEY));
		Assert.assertEquals(FAKE_SMTP_PORT, properties.get(EmailService.SMTP_PORT_KEY));
		Assert.assertEquals(mockSslFactory,
				properties.get(EmailService.SOCKET_FACTORY_KEY));
		Assert.assertEquals(FAKE_SMTP_PORT, properties.get(
				EmailService.SOCKET_FACTORY_PORT_KEY));
		Assert.assertEquals(FAKE_EMAIL_ADDRESS, properties.get(
				EmailService.MAIL_USER_KEY));
	}
	
	@Test
	public void testAuthenticationConfigured() {
		Assert.assertTrue(impl.isAuthenticationConfigured());
		impl.setUseAuth(true);
		Assert.assertFalse(impl.isAuthenticationConfigured());
		impl.setPassword(FAKE_PASSWORD);
		Assert.assertTrue(impl.isAuthenticationConfigured());
	}
	
	@Test
	public void testSendEmailWithoutAttachment() throws Exception {
		AtomicReference<Address[]> obtainedAddresses = new AtomicReference<>();
		Mockito.doAnswer(new ArgumentCatcher<Void>(v -> obtainedAddresses.set(v.get()), 1)).when(
				mimeMessageMock).setRecipients(
						Mockito.eq(Message.RecipientType.TO), Mockito.any(Address[].class));
		AtomicReference<String> obtainedSubject = new AtomicReference<>();
		Mockito.doAnswer(new ArgumentCatcher<Void>(v -> obtainedSubject.set(v.get()), 0)).when(
				mimeMessageMock).setSubject(Mockito.anyString());
		AtomicReference<MimeMultipart> obtainedMultipart = new AtomicReference<>();
		Mockito.doAnswer(new ArgumentCatcher<Void>(v -> obtainedMultipart.set(v.get()), 0)).when(
				mimeMessageMock).setContent(Mockito.any());
		impl.sendEmail(FAKE_EMAIL_ADDRESS, FAKE_SUBJECT, FAKE_EMAIL_CONTENT, null);
		Mockito.verify(mimeMessageMock).setFrom();
		PowerMockito.verifyStatic(Transport.class);
		Transport.send(mimeMessageMock);
		Assert.assertEquals(1, obtainedMultipart.get().getCount());
		Assert.assertEquals(FAKE_EMAIL_CONTENT, obtainedMultipart.get().getBodyPart(0).getContent());
		Assert.assertEquals(FAKE_SUBJECT, obtainedSubject.get());
		Assert.assertEquals(1, obtainedAddresses.get().length);
		Assert.assertEquals(FAKE_EMAIL_ADDRESS, obtainedAddresses.get()[0].toString());
	}
	
	@Test
	public void testSendEmailWithAttachment() throws Exception {
		impl.setUseAuth(true);
		impl.setPassword(FAKE_PASSWORD);
		AtomicReference<Address[]> obtainedAddresses = new AtomicReference<>();
		Mockito.doAnswer(new ArgumentCatcher<Void>(v -> obtainedAddresses.set(v.get()), 1)).when(
				mimeMessageMock).setRecipients(
						Mockito.eq(Message.RecipientType.TO), Mockito.any(Address[].class));
		AtomicReference<String> obtainedSubject = new AtomicReference<>();
		Mockito.doAnswer(new ArgumentCatcher<Void>(v -> obtainedSubject.set(v.get()), 0)).when(
				mimeMessageMock).setSubject(Mockito.anyString());
		AtomicReference<MimeMultipart> obtainedMultipart = new AtomicReference<>();
		Mockito.doAnswer(new ArgumentCatcher<Void>(v -> obtainedMultipart.set(v.get()), 0)).when(
				mimeMessageMock).setContent(Mockito.any());
		Mockito.when(mockDataSource.getName()).thenReturn(FAKE_FILE_NAME);
		impl.sendEmail(FAKE_EMAIL_ADDRESS, FAKE_SUBJECT, FAKE_EMAIL_CONTENT, mockDataSource);
		final PasswordAuthentication access = AuthenticatorAccessor.access(
				obtainedAuthenticator);
		Assert.assertEquals(FAKE_PASSWORD, access.getPassword());
		Assert.assertEquals(FAKE_EMAIL_ADDRESS, access.getUserName());
		Mockito.verify(mimeMessageMock).setFrom();
		PowerMockito.verifyStatic(Transport.class);
		Transport.send(mimeMessageMock);
		final MimeMultipart multipart = obtainedMultipart.get();
		Assert.assertEquals(2, multipart.getCount());
		Assert.assertEquals(FAKE_EMAIL_CONTENT, multipart.getBodyPart(0).getContent());
		Assert.assertEquals(FAKE_SUBJECT, obtainedSubject.get());
		Assert.assertEquals(1, obtainedAddresses.get().length);
		Assert.assertEquals(FAKE_EMAIL_ADDRESS, obtainedAddresses.get()[0].toString());
		final MimeBodyPart attachmentBodyPart = (MimeBodyPart)multipart.getBodyPart(
				1);
		Assert.assertEquals(FAKE_FILE_NAME, attachmentBodyPart.getFileName());
		Assert.assertEquals(mockDataSource, attachmentBodyPart.getDataHandler().getDataSource());
	}

}
