package com.github.pedroarrudamoreira.vaultage.root.security._2fa.filter;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.TokenManager;
import com.github.pedroarrudamoreira.vaultage.accesscontrol.TokenType;
import com.github.pedroarrudamoreira.vaultage.root.email.service.EmailService;
import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import com.github.pedroarrudamoreira.vaultage.test.util.AbstractTest;
import com.github.pedroarrudamoreira.vaultage.test.util.ObjectFactoryStatic;
import com.github.pedroarrudamoreira.vaultage.test.util.mockito.ArgumentCatcher;
import com.github.pedroarrudamoreira.vaultage.util.EventLoop;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class TwoFactorAuthFilterTest extends AbstractTest {
	
	private static final String FAKE_PASSWORD = "myp4ss";
	private static final String FAKE_TOKEN = "mytoken_1234567890";
	private static final String FAKE_HOST = "my.host.com";
	private static final String FAKE_EMAIL_ADDR = "myself@server.com";
	
	@Mock
	private AuthenticationProvider authProvider;

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
	private RequestDispatcher channelSelectorDispatcherMock;
	
	@Mock
	private EmailService emailServiceMock;
	
	@Mock
	private TokenManager tokenManager;

	@Mock
	private EventLoop eventLoop;

	@Mock
	private ObjectFactory objectFactory;

	@Mock
	@ObjectFactoryStatic(
			clazz = SecurityContextHolder.class,
			name = "getContext"
	)
	private SecurityContext securityContext;

	@Mock
	private Authentication authentication;
	
	private TwoFactorAuthFilter impl;
	
	private boolean emailEnabled = true;
	
	@Before
	public void setup() {
		impl = new TwoFactorAuthFilter();
		impl.setEnabled(true);
		impl.setServletContext(servletContextMock);
		impl.setEmailService(emailServiceMock);
		impl.setAuthProvider(authProvider);
		impl.setEventLoop(eventLoop);
		impl.setTokenManager(tokenManager);
		Mockito.when(securityContext.getAuthentication()).thenAnswer(i -> authentication);
		Mockito.when(httpServletRequestMock.getSession()).thenReturn(httpSessionMock);
		Mockito.when(servletContextMock.getRequestDispatcher(
				TwoFactorAuthFilter.CHECK_EMAIL_HTML_LOCATION)).thenReturn(checkEmailDispatcherMock);
		Mockito.when(servletContextMock.getRequestDispatcher(
				TwoFactorAuthFilter.PASSWORD_HTML_LOCATION)).thenReturn(emailPasswordDispatcherMock);
		Mockito.when(servletContextMock.getRequestDispatcher(
				TwoFactorAuthFilter.CHANNEL_SELECTOR_LOCATION)).thenReturn(channelSelectorDispatcherMock);
		Mockito.when(emailServiceMock.isEnabled()).thenAnswer(i -> emailEnabled);
	}

	@Test
	public void testDoFilter_ClearOnAnonymous() throws Exception {
		authentication = Mockito.mock(AnonymousAuthenticationToken.class);
		impl.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);
		Mockito.verify(filterChainMock).doFilter(httpServletRequestMock, httpServletResponseMock);
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
		Mockito.when(tokenManager.isTokenValid(Mockito.any(),
				Mockito.eq(TokenType.SESSION))).thenReturn(true);
		Mockito.when(tokenManager.removeToken(Mockito.any())).thenReturn(true);
		impl.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);
		Mockito.verify(channelSelectorDispatcherMock).forward(httpServletRequestMock, httpServletResponseMock);
		Mockito.verify(httpSessionMock).setAttribute(TwoFactorAuthFilter.ALREADY_VALIDATED_KEY,
				ObjectFactory.PRESENT);
		
	}

	@Test
	public void testDoFilter_PasswordEmptyAndNotProvided() throws Exception {
		Mockito.when(tokenManager.isTokenValid(Mockito.any(),
				Mockito.eq(TokenType.SESSION))).thenReturn(true);
		Mockito.when(tokenManager.removeToken(Mockito.any())).thenReturn(false);
		impl.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);
		Mockito.verify(emailPasswordDispatcherMock).forward(httpServletRequestMock,
				httpServletResponseMock);
		Mockito.verifyNoInteractions(filterChainMock);
		
	}

	@Test
	public void testDoFilter_PasswordEmptyAndProvided_EmailSent() throws Exception {
		Mockito.when(tokenManager.isTokenValid(Mockito.any(),
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
		Mockito.when(tokenManager.isTokenValid(Mockito.any(),
				Mockito.eq(TokenType.SESSION))).thenReturn(false);
		Mockito.when(tokenManager.generateNewToken(
				Mockito.eq(TokenType.SESSION))).thenReturn(FAKE_TOKEN);
		impl.setThisServerHost(FAKE_HOST);
		Mockito.when(emailServiceMock.isAuthenticationConfigured()).thenReturn(true);
		
		User user = new User();
		user.setEmail(FAKE_EMAIL_ADDR);
		Mockito.when(authProvider.getCurrentUser()).thenReturn(user);
		String[] emailContent = new String[1];
		Mockito.doAnswer((i) -> {
			i.getArgument(0, EventLoop.Task.class).run();
			return null;
		}).when(eventLoop).execute(Mockito.any());
		Mockito.doAnswer(new ArgumentCatcher<Void>(v -> emailContent[0] = v.get(), 2)).when(
				emailServiceMock).sendEmail(Mockito.eq(FAKE_EMAIL_ADDR), Mockito.eq(TwoFactorAuthFilter.SUBJECT), Mockito.any(),
						Mockito.eq(null));
		impl.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);
		Mockito.verify(checkEmailDispatcherMock).forward(httpServletRequestMock,
				httpServletResponseMock);
		Mockito.verifyNoInteractions(filterChainMock);
		Mockito.verify(httpSessionMock).setAttribute(TwoFactorAuthFilter.EMAIL_SENT_KEY, ObjectFactory.PRESENT);
		Assert.assertTrue("wrong email content", emailContent[0].contains("A login attempt has been made on your Vaultage server."));
		Assert.assertTrue("no token in email", emailContent[0].contains(FAKE_TOKEN));
	}

}
