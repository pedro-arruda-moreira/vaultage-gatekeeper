package com.github.pedroarrudamoreira.vaultage.root.filter._2fa;

import java.io.StringWriter;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.mail.Authenticator;
import javax.mail.AuthenticatorAccessor;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

import com.github.pedroarrudamoreira.vaultage.accesscontrol.TokenManager;
import com.github.pedroarrudamoreira.vaultage.accesscontrol.TokenType;
import com.github.pedroarrudamoreira.vaultage.root.filter._2fa.ssl.EasySSLSocketFactory;
import com.github.pedroarrudamoreira.vaultage.test.util.TestUtils;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ObjectFactory.class, TokenManager.class, Session.class, Transport.class})
public class TwoFactorAuthFilterTest {
	
	private static final String FAKE_EMAIL_CONTENT = "hello!";
	private static final String STRING_FALSE = "false";
	private static final String STRING_TRUE = "true";
	private static final String FAKE_SMTP_PORT = "1234";
	private static final String FAKE_PASSWORD = "myp4ss";
	private static final String FAKE_SMTP_HOST = "mail.test.com";
	private static final String FAKE_EMAIL_ADDRESS = "test@test.com";
	private Properties properties;
	
	@Mock
	private FilterChain filterChainMock;
	
	@Mock
	private HttpServletRequest httpServletRequestMock;
	
	@Mock
	private HttpServletResponse httpServletResponseMock;
	
	@Mock
	private HttpSession httpSessionMock;
	
	@Mock
	private ServletContext servletContextMock;
	
	@Mock
	private RequestDispatcher emailPasswordDispatcherMock;
	
	@Mock
	private RequestDispatcher checkEmailDispatcherMock;
	
	@Mock
	private RequestDispatcher emailTemplateDispatcherMock;
	
	@Mock
	private Session emailSessionMock;
	
	@Mock
	private MimeMessage mimeMessageMock;
	
	@Mock
	private EasySSLSocketFactory mockSslFactory;
	
	private StringWriter stringWriterMock;
	
	private Authenticator obtainedAuthenticator;
	
	private TwoFactorAuthFilter impl;
	
	@BeforeClass
	public static void setupStatic() {
		TestUtils.doPrepareForTest();
	}
	
	@Before
	public void setup() {
		TestUtils.doPrepareForTest();
		properties = new Properties();
		stringWriterMock = new StringWriter();
		impl = new TwoFactorAuthFilter();
		impl.setServletContext(servletContextMock);
		impl.setSslContextFactory(mockSslFactory);
		PowerMockito.when(ObjectFactory.buildStringWriter()).thenReturn(stringWriterMock);
		PowerMockito.when(ObjectFactory.buildMimeMessage(emailSessionMock)).thenReturn(mimeMessageMock);
		PowerMockito.when(ObjectFactory.buildProperties()).thenReturn(properties);
		PowerMockito.when(ObjectFactory.buildEmailSession(Mockito.any(), Mockito.any())).then((inv) -> {
			obtainedAuthenticator = inv.getArgument(1, Authenticator.class);
			return emailSessionMock;
		});
		Mockito.when(httpServletRequestMock.getSession()).thenReturn(httpSessionMock);
		Mockito.when(servletContextMock.getRequestDispatcher(
				TwoFactorAuthFilter.CHECK_EMAIL_HTML_LOCATION)).thenReturn(checkEmailDispatcherMock);
		Mockito.when(servletContextMock.getRequestDispatcher(
				TwoFactorAuthFilter.EMAIL_TEMPLATE_JSP_LOCATION)).thenReturn(emailTemplateDispatcherMock);
		Mockito.when(servletContextMock.getRequestDispatcher(
				TwoFactorAuthFilter.PASSWORD_HTML_LOCATION)).thenReturn(emailPasswordDispatcherMock);
	}
	
	@Test
	public void testAfterPropertiesSet_NotEnabled() throws Exception {
		impl.setEnabled(false);
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
		Assert.assertEquals(STRING_FALSE, properties.get(TwoFactorAuthFilter.USE_START_TLS_KEY));
		Assert.assertEquals(STRING_TRUE, properties.get(TwoFactorAuthFilter.USE_AUTH_KEY));
		Assert.assertEquals(FAKE_SMTP_HOST, properties.get(TwoFactorAuthFilter.SMTP_HOST_KEY));
		Assert.assertEquals(FAKE_SMTP_PORT, properties.get(TwoFactorAuthFilter.SMTP_PORT_KEY));
		Assert.assertEquals(mockSslFactory,
				properties.get(TwoFactorAuthFilter.SOCKET_FACTORY_KEY));
		Assert.assertEquals(FAKE_SMTP_PORT, properties.get(
				TwoFactorAuthFilter.SOCKET_FACTORY_PORT_KEY));
		Assert.assertEquals(FAKE_EMAIL_ADDRESS, properties.get(
				TwoFactorAuthFilter.MAIL_USER_KEY));
	}
	
	@Test
	public void testDoFilter_NotEnabled() throws Exception {
		impl.setEnabled(false);
		impl.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);
		Mockito.verify(filterChainMock).doFilter(httpServletRequestMock, httpServletResponseMock);
	}
	
	@Test
	public void testDoFilter_AlreadyValidated() throws Exception {
		impl.setEnabled(true);
		Mockito.when(httpSessionMock.getAttribute(
				TwoFactorAuthFilter.ALREADY_VALIDATED_KEY)).thenReturn(impl);
		impl.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);
		Mockito.verify(filterChainMock).doFilter(httpServletRequestMock, httpServletResponseMock);
		
	}

	@Test
	public void testDoFilter_ValidToken() throws Exception {
		Mockito.when(httpServletRequestMock.getParameter(
				TwoFactorAuthFilter.EMAIL_TOKEN_KEY)).thenReturn(UUID.randomUUID().toString());
		PowerMockito.when(TokenManager.isTokenValid(Mockito.any(),
				Mockito.eq(TokenType.SESSION))).thenReturn(true);
		PowerMockito.when(TokenManager.removeToken(Mockito.any())).thenReturn(true);
		impl.setEnabled(true);
		impl.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);
		Mockito.verify(filterChainMock).doFilter(httpServletRequestMock, httpServletResponseMock);
		Mockito.verify(httpSessionMock).setAttribute(TwoFactorAuthFilter.ALREADY_VALIDATED_KEY,
				impl);
		
	}

	@Test
	public void testDoFilter_PasswordEmptyAndNotProvided() throws Exception {
		PowerMockito.when(TokenManager.isTokenValid(Mockito.any(),
				Mockito.eq(TokenType.SESSION))).thenReturn(true);
		PowerMockito.when(TokenManager.removeToken(Mockito.any())).thenReturn(false);
		impl.setEnabled(true);
		impl.setUseAuth(true);
		impl.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);
		Mockito.verify(emailPasswordDispatcherMock).forward(httpServletRequestMock,
				httpServletResponseMock);
		Mockito.verifyNoInteractions(filterChainMock);
		
	}

	@Test
	public void testDoFilter_PasswordEmptyAndProvided_EmailSent() throws Exception {
		PowerMockito.when(TokenManager.isTokenValid(Mockito.any(),
				Mockito.eq(TokenType.SESSION))).thenReturn(false);
		Mockito.when(httpServletRequestMock.getParameter(
				TwoFactorAuthFilter.EMAIL_PASSWORD_REQUEST_KEY)).thenReturn(FAKE_PASSWORD);
		Mockito.when(httpSessionMock.getAttribute(
				TwoFactorAuthFilter.EMAIL_SENT_KEY)).thenReturn(impl);
		impl.setEnabled(true);
		impl.setUseAuth(true);
		impl.setSmtpUsername(FAKE_EMAIL_ADDRESS);
		impl.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);
		PasswordAuthentication obtainedUsernamePassword = AuthenticatorAccessor.access(
				obtainedAuthenticator);
		Assert.assertEquals(FAKE_PASSWORD, obtainedUsernamePassword.getPassword());
		Assert.assertEquals(FAKE_EMAIL_ADDRESS, obtainedUsernamePassword.getUserName());
		
		Mockito.verify(checkEmailDispatcherMock).forward(httpServletRequestMock,
				httpServletResponseMock);
		Mockito.verifyNoInteractions(filterChainMock);
	}

	@Test
	public void testDoFilter_NoAuth_MustSendEmail() throws Exception {
		PowerMockito.when(TokenManager.isTokenValid(Mockito.any(),
				Mockito.eq(TokenType.SESSION))).thenReturn(false);
		PowerMockito.when(TokenManager.generateNewToken(
				Mockito.eq(TokenType.SESSION))).thenReturn(FAKE_PASSWORD);
		Mockito.doAnswer(inv -> {
			inv.getArgument(1, HttpServletResponse.class).getWriter().write(FAKE_EMAIL_CONTENT);
			return null;
		}).when(emailTemplateDispatcherMock).include(Mockito.eq(httpServletRequestMock),
				Mockito.any());
		impl.setEnabled(true);
		impl.setSmtpUsername(FAKE_EMAIL_ADDRESS);
		impl.setAddressToSend(FAKE_EMAIL_ADDRESS);
		impl.setThisServerHost(TwoFactorAuthFilterTest.FAKE_SMTP_HOST);
		impl.setUseAuth(false);
		AtomicBoolean serverHostDefined = new AtomicBoolean(false);
		AtomicBoolean tokenDefined = new AtomicBoolean(false);
		Mockito.doAnswer((i) -> {
			serverHostDefined.set(true);
			return null;
		}).when(httpServletRequestMock).setAttribute(Mockito.eq(TwoFactorAuthFilter.EMAIL_TEMPLATE_SERVER_HOST_KEY),
				Mockito.eq("http://mail.test.com"));
		
		Mockito.doAnswer((i) -> {
			tokenDefined.set(true);
			return null;
		}).when(httpServletRequestMock).setAttribute(Mockito.eq(TwoFactorAuthFilter.EMAIL_TOKEN_KEY),
				Mockito.eq("myp4ss"));
		
		impl.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);
		Mockito.verify(mimeMessageMock).setFrom();
		Mockito.verify(checkEmailDispatcherMock).forward(httpServletRequestMock,
				httpServletResponseMock);
		Assert.assertTrue("Server host for email was not defined.", serverHostDefined.get());
		Assert.assertTrue("Token for email was not defined.", tokenDefined.get());
		Mockito.verify(mimeMessageMock).setContent(FAKE_EMAIL_CONTENT,
				TwoFactorAuthFilter.EMAIL_CONTENT_TYPE);
		Mockito.verifyNoInteractions(filterChainMock);
		Mockito.verify(httpSessionMock).setAttribute(TwoFactorAuthFilter.EMAIL_SENT_KEY, impl);
		PowerMockito.verifyStatic(Transport.class);
		Transport.send(mimeMessageMock);
		Assert.assertEquals(FAKE_EMAIL_CONTENT, stringWriterMock.toString());
	}

}
