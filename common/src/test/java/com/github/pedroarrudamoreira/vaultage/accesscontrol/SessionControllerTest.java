package com.github.pedroarrudamoreira.vaultage.accesscontrol;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequestEvent;
import javax.servlet.SessionCookieConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.cglib.proxy.UndeclaredThrowableException;
import org.springframework.security.core.context.SecurityContext;

import com.github.pedroarrudamoreira.vaultage.test.util.TestUtils;
import com.github.pedroarrudamoreira.vaultage.test.util.mockito.ArgumentCatcher;
import com.github.pedroarrudamoreira.vaultage.util.EventLoop;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

@RunWith(PowerMockRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@PrepareForTest({ObjectFactory.class, EventLoop.class})
@PowerMockRunnerDelegate(JUnit4.class)
public class SessionControllerTest {

	@Mock
	private HttpSession httpSessionMock;

	@Mock
	private ServletContext servletContextMock;

	@Mock
	private SessionCookieConfig sessionCookieConfigMock;

	@Mock
	private HttpServletRequest httpServletRequestMock;

	@Mock
	private SecurityContext securityContextMock;

	@Mock
	private FilterChain filterChainMock;

	private SessionController impl;

	private static AtomicInteger remainingPerHour = new AtomicInteger(10);

	private static AtomicInteger remainingPerDay = new AtomicInteger(10);

	private static Supplier<Boolean> obtainedRunnable;
	@BeforeClass
	public static void setupStatic() throws Exception {
		TestUtils.doPrepareForTest();
		PowerMockito.doAnswer(new ArgumentCatcher<Void>(null, v -> obtainedRunnable = v.get(), 0)).when(EventLoop.class);
		EventLoop.repeatTask(Mockito.any(), Mockito.eq(1l), Mockito.any());
		PowerMockito.when(ObjectFactory.buildAtomicInteger(500)).thenReturn(remainingPerHour);
		PowerMockito.when(ObjectFactory.buildAtomicInteger(900)).thenReturn(remainingPerDay);
	}

	@Before
	public void setup() throws Exception {
		setupStatic();
		impl = new SessionController();
		SessionController.setMaxLoginAttemptsPerSession(3);
		SessionController.setMaxLoginAttemptsPerSession(2);
		Mockito.when(httpServletRequestMock.getSession()).thenReturn(httpSessionMock);
	}
	@Test
	public void test000CheckTaskCreation() {
		Assert.assertNotNull(obtainedRunnable);
	}

	@Test
	public void test001DefineLimitsOnlyOnce() {
		SessionController.setMaxSessionsPerDay(5);
		SessionController.setMaxSessionsPerDay(6);
		SessionController.setMaxSessionsPerHour(3);
		SessionController.setMaxSessionsPerHour(4);
		Assert.assertEquals(5, remainingPerDay.get());
		Assert.assertEquals(3, remainingPerHour.get());
	}

	@Test
	public void test002CreateSessionsExpireHour() {
		impl.sessionCreated(new HttpSessionEvent(httpSessionMock));
		impl.sessionCreated(new HttpSessionEvent(httpSessionMock));
		impl.sessionCreated(new HttpSessionEvent(httpSessionMock));
		try {
			impl.sessionCreated(new HttpSessionEvent(httpSessionMock));
			Assert.fail("exception expected.");
		} catch (UndeclaredThrowableException e1) {
			Mockito.verify(httpSessionMock).invalidate();
			Assert.assertEquals(SecurityException.class, e1.getCause().getClass());
		}
	}

	@Test
	public void test003CreateSessionsExpireDay() {
		doExecuteRunnable(1);
		impl.sessionCreated(new HttpSessionEvent(httpSessionMock));
		try {
			impl.sessionCreated(new HttpSessionEvent(httpSessionMock));
			Assert.fail("exception expected.");
		} catch (UndeclaredThrowableException e1) {
			Mockito.verify(httpSessionMock).invalidate();
			Assert.assertEquals(SecurityException.class, e1.getCause().getClass());
		}
	}

	@Test
	public void test004NewDay() {
		resetAttempts();
		impl.sessionCreated(new HttpSessionEvent(httpSessionMock));
		impl.sessionCreated(new HttpSessionEvent(httpSessionMock));
		Mockito.verify(httpSessionMock, Mockito.never()).invalidate();
	}


	@Test
	public void test005ContextAware() {
		Mockito.when(servletContextMock.getSessionCookieConfig()).thenReturn(
				sessionCookieConfigMock);
		impl.setSessionDurationInHours(1);
		impl.setSecure(false);
		impl.setServletContext(servletContextMock);
		Mockito.verify(servletContextMock).setSessionTimeout(60);
		Mockito.verify(sessionCookieConfigMock).setSecure(false);
		Mockito.verify(sessionCookieConfigMock).setMaxAge(3600);
	}

	@Test
	public void test006AttemptsPerSession() {

		resetAttempts();
		AtomicInteger[] attempts = new AtomicInteger[1];
		configureAttempts(attempts);
		impl.sessionCreated(new HttpSessionEvent(httpSessionMock));
		impl.setSecure(false);
		ServletRequestEvent sre = new ServletRequestEvent(
				servletContextMock, httpServletRequestMock);
		impl.requestInitialized(sre);
		impl.requestInitialized(sre);
		impl.requestInitialized(sre);
		impl.requestInitialized(sre);
		Mockito.verify(httpSessionMock).invalidate();

	}
	@Test
	public void test007SecureWithNonSecureRequest() {

		resetAttempts();
		AtomicInteger[] attempts = new AtomicInteger[1];
		Mockito.when(httpServletRequestMock.isSecure()).thenReturn(false);
		configureAttempts(attempts);
		impl.sessionCreated(new HttpSessionEvent(httpSessionMock));
		impl.setSecure(true);
		ServletRequestEvent sre = new ServletRequestEvent(
				servletContextMock, httpServletRequestMock);
		try {
			impl.requestInitialized(sre);
			Assert.fail("expected exception.");
		} catch (UndeclaredThrowableException e) {
			Mockito.verify(httpSessionMock).invalidate();
			Assert.assertEquals(SecurityException.class, e.getCause().getClass());
		}

	}
	@Test
	public void test008SecureWithSecureRequest() {

		resetAttempts();
		AtomicInteger[] attempts = new AtomicInteger[1];
		Mockito.when(httpServletRequestMock.isSecure()).thenReturn(true);
		final String fakeUrl = "url";
		Mockito.when(httpServletRequestMock.getRequestURI()).thenReturn(fakeUrl);
		Mockito.when(httpServletRequestMock.getAttribute(
				SessionController.ORIGINAL_URL)).thenReturn(fakeUrl);
		configureAttempts(attempts);
		impl.sessionCreated(new HttpSessionEvent(httpSessionMock));
		impl.setSecure(true);
		ServletRequestEvent sre = new ServletRequestEvent(
				servletContextMock, httpServletRequestMock);
		impl.requestInitialized(sre);
		Assert.assertEquals(2, attempts[0].intValue());
		Assert.assertEquals(httpServletRequestMock, SessionController.getCurrentRequest());
		Assert.assertEquals(fakeUrl, SessionController.getOriginalUrl());

	}
	@Test
	public void test009AlreadyAuthenticated() {

		resetAttempts();
		AtomicInteger[] attempts = new AtomicInteger[1];
		Mockito.when(httpServletRequestMock.isSecure()).thenReturn(true);
		Mockito.when(httpSessionMock.getAttribute("__logged_on__$$")).thenReturn("true");
		configureAttempts(attempts);
		impl.sessionCreated(new HttpSessionEvent(httpSessionMock));
		impl.setSecure(true);
		ServletRequestEvent sre = new ServletRequestEvent(
				servletContextMock, httpServletRequestMock);
		impl.requestInitialized(sre);
		Assert.assertEquals(3, attempts[0].intValue());

	}

	@Test
	public void test010RequestDestroyed() {
		impl.requestDestroyed(new ServletRequestEvent(servletContextMock, httpServletRequestMock));
		Assert.assertNull(SessionController.getCurrentRequest());
	}

	@Test
	public void test011LoginSuccessful() throws Exception {
		impl.doFilter(httpServletRequestMock, null, filterChainMock);
		Mockito.verify(httpSessionMock).setAttribute(SessionController.LOGGED_ON_KEY, ObjectFactory.PRESENT);
		Mockito.verify(filterChainMock).doFilter(httpServletRequestMock, null);
	}

	private void configureAttempts(AtomicInteger[] attempts) {
		Mockito.doAnswer(new ArgumentCatcher<Void>(v -> attempts[0] = v.get(), 1)).when(
				httpSessionMock).setAttribute(Mockito.eq("login_attempts_remaining"),
						Mockito.any());
		Mockito.when(httpSessionMock.getAttribute(
				"login_attempts_remaining")).then(inv -> attempts[0]);
	}
	
	private void doExecuteRunnable(int count) {
		for(int i = 0; i < count; i++) {
			Assert.assertTrue("should always return true", obtainedRunnable.get());
		}
	}

	private void resetAttempts() {
		doExecuteRunnable(24);
	}



}
