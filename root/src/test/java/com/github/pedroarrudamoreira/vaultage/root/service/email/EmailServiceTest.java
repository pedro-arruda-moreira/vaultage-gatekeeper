package com.github.pedroarrudamoreira.vaultage.root.service.email;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

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

import com.github.pedroarrudamoreira.vaultage.root.service.email.EmailService;
import com.github.pedroarrudamoreira.vaultage.root.service.email.util.EasySSLSocketFactory;
import com.github.pedroarrudamoreira.vaultage.test.util.TestUtils;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;
@RunWith(PowerMockRunner.class)
@PrepareForTest({ObjectFactory.class, Session.class, Transport.class})
public class EmailServiceTest {
	private static final String FAKE_EMAIL_CONTENT = "hello!";
	private static final String STRING_FALSE = "false";
	private static final String STRING_TRUE = "true";
	private static final String FAKE_SMTP_PORT = "1234";
	private static final String FAKE_PASSWORD = "myp4ss";
	private static final String FAKE_SMTP_HOST = "mail.test.com";
	private static final String FAKE_EMAIL_ADDRESS = "test@test.com";
	
	private EmailService impl;
	
	private Authenticator obtainedAuthenticator;

	private Properties properties;
	
	@Mock
	private Session emailSessionMock;
	
	@Mock
	private MimeMessage mimeMessageMock;
	
	@Mock
	private EasySSLSocketFactory mockSslFactory;
	
	@BeforeClass
	public static void setupStatic() {
		TestUtils.doPrepareForTest();
	}
	
	@Before
	public void setup() {
		impl = new EmailService();
		impl.setSslContextFactory(mockSslFactory);
		properties = new Properties();
		PowerMockito.when(ObjectFactory.buildMimeMessage(emailSessionMock)).thenReturn(mimeMessageMock);
		PowerMockito.when(ObjectFactory.buildProperties()).thenReturn(properties);
		PowerMockito.when(ObjectFactory.buildEmailSession(Mockito.any(), Mockito.any())).then((inv) -> {
			obtainedAuthenticator = inv.getArgument(1, Authenticator.class);
			return emailSessionMock;
		});
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
		impl.afterPropertiesSet();
	}

	@Test
	public void testAfterPropertiesSet_OK() throws Exception {
		impl.setAddressToSend(FAKE_EMAIL_ADDRESS);
		impl.setDebug(false);
		impl.setPassword(FAKE_PASSWORD);
		impl.setSmtpHost(FAKE_SMTP_HOST);
		impl.setSmtpPort(FAKE_SMTP_PORT);
		impl.setSmtpUsername(FAKE_EMAIL_ADDRESS);
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

}
