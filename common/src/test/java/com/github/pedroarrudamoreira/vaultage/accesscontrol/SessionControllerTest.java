package com.github.pedroarrudamoreira.vaultage.accesscontrol;

import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletContext;
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

import com.github.pedroarrudamoreira.vaultage.test.util.TestUtils;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;
import com.github.pedroarrudamoreira.vaultage.util.ThreadControl;

@RunWith(PowerMockRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@PrepareForTest({ObjectFactory.class, ThreadControl.class})
@PowerMockRunnerDelegate(JUnit4.class)
public class SessionControllerTest {

	@Mock
	private HttpSession sessionMock;

	@Mock
	private ServletContext servletContextMock;

	private SessionController impl;

	private static int sleepCount;

	private static boolean isStarted = false;
	private static Thread mockThread = new Thread() {
		public synchronized void start() {
			isStarted = true;
		}
	};

	private static AtomicInteger remainingPerHour = new AtomicInteger(10);

	private static AtomicInteger remainingPerDay = new AtomicInteger(10);

	private static Runnable obtainedRunnable;

	private static class Stop extends RuntimeException {

	}
	@BeforeClass
	public static void setupStatic() throws Exception {
		TestUtils.doPrepareForTest();
		PowerMockito.when(ObjectFactory.buildThread(Mockito.any(), Mockito.any())).then((inv) -> {
			obtainedRunnable = inv.getArgument(0, Runnable.class);
			return mockThread;
		});
		PowerMockito.doAnswer((inv) -> {
			sleepCount--;
			if(sleepCount < 0) {
				throw new Stop();
			}
			return null;
		}).when(ThreadControl.class);
		ThreadControl.sleep(Mockito.anyLong());
		PowerMockito.when(ObjectFactory.buildAtomicInteger(500)).thenReturn(remainingPerHour);
		PowerMockito.when(ObjectFactory.buildAtomicInteger(900)).thenReturn(remainingPerDay);
	}

	@Before
	public void setup() throws Exception {
		setupStatic();
		impl = new SessionController();
	}
	@Test
	public void test000CheckThreadCreation() {
		Assert.assertTrue(isStarted);
		Assert.assertTrue(mockThread.isDaemon());
	}

	@Test
	public void test001DefineLimitsOnlyOnce() {
		impl.setMaxSessionsPerDay(5);
		impl.setMaxSessionsPerDay(6);
		impl.setMaxSessionsPerHour(3);
		impl.setMaxSessionsPerHour(4);
		Assert.assertEquals(5, remainingPerDay.get());
		Assert.assertEquals(3, remainingPerHour.get());
	}

	@Test
	public void test002CreateSessionsExpireHour() {
		impl.sessionCreated(new HttpSessionEvent(sessionMock));
		impl.sessionCreated(new HttpSessionEvent(sessionMock));
		impl.sessionCreated(new HttpSessionEvent(sessionMock));
		try {
			impl.sessionCreated(new HttpSessionEvent(sessionMock));
			Assert.fail("exception expected.");
		} catch (UndeclaredThrowableException e1) {
			Mockito.verify(sessionMock).invalidate();
			Assert.assertEquals(IllegalAccessException.class, e1.getCause().getClass());
		}
	}

	@Test
	public void test003CreateSessionsExpireDay() {
		try {
			sleepCount = 1;
			obtainedRunnable.run();
			Assert.fail("did not stop");
		} catch (Stop e) {
			impl.sessionCreated(new HttpSessionEvent(sessionMock));
			try {
				impl.sessionCreated(new HttpSessionEvent(sessionMock));
				Assert.fail("exception expected.");
			} catch (UndeclaredThrowableException e1) {
				Mockito.verify(sessionMock).invalidate();
				Assert.assertEquals(IllegalAccessException.class, e1.getCause().getClass());
			}
		}
	}

	@Test
	public void test004NewDay() {
		try {
			sleepCount = 24;
			obtainedRunnable.run();
			Assert.fail("did not stop");
		} catch (Stop e) {
			impl.sessionCreated(new HttpSessionEvent(sessionMock));
			impl.sessionCreated(new HttpSessionEvent(sessionMock));
			Mockito.verify(sessionMock, Mockito.never()).invalidate();
		}
	}


	@Test
	public void test005ContextAware() {
		impl.setServletContext(servletContextMock);
		Mockito.verify(servletContextMock).addListener(SessionController.class);
	}



}
