package com.github.pedroarrudamoreira.vaultage.root.boot.listener;

import java.util.HashMap;
import java.util.function.Supplier;

import javax.servlet.ServletContext;

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
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;
import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import com.github.pedroarrudamoreira.vaultage.root.server.VaultageServerManager;
import com.github.pedroarrudamoreira.vaultage.test.util.TestUtils;
import com.github.pedroarrudamoreira.vaultage.util.EventLoop;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SessionController.class, EventLoop.class})
public class StarterTest {
	@Mock
	private AuthenticationProvider userProviderMock;

	@Mock
	private VaultageServerManager serverManagerMock;

	@Mock
	private ServletContext servletContextMock;

	@Mock
	private ApplicationContext applicationContextMock;

	@Mock
	private Environment environmentMock;

	@Mock
	private EventLoop eventLoop;

	private Starter impl;

	private Supplier<Boolean> obtainedFunction;

	@BeforeClass
	public static void setupStatic() {
		TestUtils.doPrepareForTest();
	}

	@Before
	public void setup() {
		setupStatic();
		obtainedFunction = null;
		impl = new Starter();
		impl.setEventLoop(eventLoop);
		impl.setServerManager(serverManagerMock);
		impl.setUserProvider(userProviderMock);
		Mockito.when(applicationContextMock.getEnvironment()).thenReturn(environmentMock);
		Mockito.doAnswer(i -> {
			obtainedFunction = i.getArgument(0, Supplier.class);
			return null;
		}).when(eventLoop).repeatTask(Mockito.any(), Mockito.anyLong(), Mockito.any());
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_duplicatePorts() {
		final HashMap<String, User> userMap = new HashMap<>();
		userMap.put("u1", new User());
		userMap.put("u2", new User());
		Mockito.when(userProviderMock.getUsers()).thenReturn(userMap);
		Mockito.when(environmentMock.resolvePlaceholders(Mockito.any())).thenReturn("d1").thenReturn("d2");
		PowerMockito.when(SessionController.getApplicationContext()).thenReturn(null).thenReturn(applicationContextMock);
		impl.setServletContext(servletContextMock);
		Assert.assertTrue(obtainedFunction.get());
		obtainedFunction.get();
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_duplicateDataDirs() {
		final HashMap<String, User> userMap = new HashMap<>();
		final User u1 = new User();
		u1.setPort(4000);
		userMap.put("u1", u1);
		userMap.put("u2", new User());
		Mockito.when(userProviderMock.getUsers()).thenReturn(userMap);
		Mockito.when(environmentMock.resolvePlaceholders(Mockito.any())).thenReturn("d1");
		PowerMockito.when(SessionController.getApplicationContext()).thenReturn(null).thenReturn(applicationContextMock);
		impl.setServletContext(servletContextMock);
		Assert.assertTrue(obtainedFunction.get());
		obtainedFunction.get();
	}

	@Test
	public void test_Ok() {
		final HashMap<String, User> userMap = new HashMap<>();
		final User u1 = new User();
		u1.setPort(4000);
		userMap.put("u1", u1);
		userMap.put("u2", new User());
		Mockito.when(userProviderMock.getUsers()).thenReturn(userMap);
		Mockito.when(environmentMock.resolvePlaceholders(Mockito.any())).thenReturn("d1").thenReturn("d2");
		PowerMockito.when(SessionController.getApplicationContext()).thenReturn(null).thenReturn(applicationContextMock);
		impl.setServletContext(servletContextMock);
		Assert.assertTrue(obtainedFunction.get());
		Assert.assertFalse(obtainedFunction.get());
		Mockito.verify(serverManagerMock).doStartAndMonitorVaultageServers();
	}

}
