package com.github.pedroarrudamoreira.vaultage.root.filter._2fa;

import java.io.StringWriter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
import com.github.pedroarrudamoreira.vaultage.root.service.email.EmailService;
import com.github.pedroarrudamoreira.vaultage.test.util.TestUtils;
import com.github.pedroarrudamoreira.vaultage.test.util.mockito.ArgumentCatcher;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ObjectFactory.class, TokenManager.class})
public class TwoFactorAuthFilterTest {
	
	private static final String FAKE_EMAIL_CONTENT = "hello!";
	private static final String FAKE_PASSWORD = "myp4ss";
	private static final String FAKE_TOKEN = "1234567890";
	private static final String FAKE_HOST = "my.host.com";

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
	private EmailService emailServiceMock;
	
	private StringWriter stringWriterMock;
	
	private TwoFactorAuthFilter impl;
	
	private boolean emailEnabled = true;
	
	@BeforeClass
	public static void setupStatic() {
		TestUtils.doPrepareForTest();
	}
	
	@Before
	public void setup() {
		TestUtils.doPrepareForTest();
		stringWriterMock = new StringWriter();
		impl = new TwoFactorAuthFilter();
		impl.setEnabled(true);
		impl.setServletContext(servletContextMock);
		impl.setEmailService(emailServiceMock);
		PowerMockito.when(ObjectFactory.buildStringWriter()).thenReturn(stringWriterMock);
		Mockito.when(httpServletRequestMock.getSession()).thenReturn(httpSessionMock);
		Mockito.when(servletContextMock.getRequestDispatcher(
				TwoFactorAuthFilter.CHECK_EMAIL_HTML_LOCATION)).thenReturn(checkEmailDispatcherMock);
		Mockito.when(servletContextMock.getRequestDispatcher(
				TwoFactorAuthFilter.EMAIL_TEMPLATE_JSP_LOCATION)).thenReturn(emailTemplateDispatcherMock);
		Mockito.when(servletContextMock.getRequestDispatcher(
				TwoFactorAuthFilter.PASSWORD_HTML_LOCATION)).thenReturn(emailPasswordDispatcherMock);
		Mockito.when(emailServiceMock.isEnabled()).thenAnswer(i -> emailEnabled);
	}
	
	@Test
	public void testDoFilter_NotEnabled() throws Exception {
		impl.setEnabled(false);
		impl.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);
		Mockito.verify(filterChainMock).doFilter(httpServletRequestMock, httpServletResponseMock);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testDoFilter_EmailServiceNotEnabled() throws Exception {
		emailEnabled = false;
		impl.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);
	}
	
	@Test
	public void testDoFilter_AlreadyValidated() throws Exception {
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
		impl.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);
		Mockito.verify(filterChainMock).doFilter(httpServletRequestMock, httpServletResponseMock);
		Mockito.verify(httpSessionMock).setAttribute(TwoFactorAuthFilter.ALREADY_VALIDATED_KEY,
				ObjectFactory.PRESENT);
		
	}

	@Test
	public void testDoFilter_PasswordEmptyAndNotProvided() throws Exception {
		PowerMockito.when(TokenManager.isTokenValid(Mockito.any(),
				Mockito.eq(TokenType.SESSION))).thenReturn(true);
		PowerMockito.when(TokenManager.removeToken(Mockito.any())).thenReturn(false);
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
		final AtomicReference<String> obtainedPass = new AtomicReference<>();
		Mockito.doAnswer(new ArgumentCatcher<Void>(v -> obtainedPass.set(v.get()), 0)).when(
				emailServiceMock).setPassword(Mockito.anyString());
		Mockito.when(emailServiceMock.isAuthenticationConfigured()).thenReturn(false);
		impl.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);
		Assert.assertEquals(FAKE_PASSWORD, obtainedPass.get());
		
		Mockito.verify(checkEmailDispatcherMock).forward(httpServletRequestMock,
				httpServletResponseMock);
		Mockito.verifyNoInteractions(filterChainMock);
	}

	@Test
	public void testDoFilter_NoAuth_MustSendEmail() throws Exception {
		PowerMockito.when(TokenManager.isTokenValid(Mockito.any(),
				Mockito.eq(TokenType.SESSION))).thenReturn(false);
		PowerMockito.when(TokenManager.generateNewToken(
				Mockito.eq(TokenType.SESSION))).thenReturn(FAKE_TOKEN);
		Mockito.doAnswer(inv -> {
			inv.getArgument(1, HttpServletResponse.class).getWriter().write(FAKE_EMAIL_CONTENT);
			return null;
		}).when(emailTemplateDispatcherMock).include(Mockito.eq(httpServletRequestMock),
				Mockito.any());
		impl.setThisServerHost(FAKE_HOST);
		AtomicBoolean serverHostDefined = new AtomicBoolean(false);
		AtomicBoolean tokenDefined = new AtomicBoolean(false);
		Mockito.when(emailServiceMock.isAuthenticationConfigured()).thenReturn(true);
		Mockito.doAnswer(i -> {
			serverHostDefined.set(true);
			return null;
		}).when(httpServletRequestMock).setAttribute(Mockito.eq(TwoFactorAuthFilter.EMAIL_TEMPLATE_SERVER_HOST_KEY),
				Mockito.eq(String.format("http://%s", FAKE_HOST)));
		
		Mockito.doAnswer(i -> {
			tokenDefined.set(true);
			return null;
		}).when(httpServletRequestMock).setAttribute(Mockito.eq(TwoFactorAuthFilter.EMAIL_TOKEN_KEY),
				Mockito.eq(FAKE_TOKEN));
		
		impl.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);
		Mockito.verify(emailServiceMock).sendEmail(TwoFactorAuthFilter.SUBJECT, FAKE_EMAIL_CONTENT, null);
		Mockito.verify(checkEmailDispatcherMock).forward(httpServletRequestMock,
				httpServletResponseMock);
		Assert.assertTrue("Server host for email was not defined.", serverHostDefined.get());
		Assert.assertTrue("Token for email was not defined.", tokenDefined.get());
		Mockito.verifyNoInteractions(filterChainMock);
		Mockito.verify(httpSessionMock).setAttribute(TwoFactorAuthFilter.EMAIL_SENT_KEY, ObjectFactory.PRESENT);
		Assert.assertEquals(FAKE_EMAIL_CONTENT, stringWriterMock.toString());
	}

}
