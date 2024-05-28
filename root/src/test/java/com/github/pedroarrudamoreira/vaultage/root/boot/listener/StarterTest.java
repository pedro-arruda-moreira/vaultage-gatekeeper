package com.github.pedroarrudamoreira.vaultage.root.boot.listener;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;
import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import com.github.pedroarrudamoreira.vaultage.root.server.VaultageServerManager;
import com.github.pedroarrudamoreira.vaultage.test.util.AbstractTest;
import com.github.pedroarrudamoreira.vaultage.util.EventLoop;
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

import java.util.HashMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SessionController.class})
public class StarterTest extends AbstractTest {
	@Mock
	private AuthenticationProvider userProviderMock;

	@Mock
	private VaultageServerManager serverManagerMock;

	@Mock
	private ApplicationContext applicationContextMock;

	@Mock
	private Environment environmentMock;

	@Mock
	private EventLoop eventLoop;

	private Starter impl;

	private EventLoop.Task obtainedFunction;

	@BeforeClass
	public static void setupStatic() {
		AbstractTest.prepareMockStatic();
	}

	@Before
	public void setup() {
		setupStatic();
		obtainedFunction = null;
		impl = new Starter();
		impl.setEventLoop(eventLoop);
		impl.setServerManager(serverManagerMock);
		impl.setUserProvider(userProviderMock);
		PowerMockito.when(SessionController.getApplicationContext()).thenReturn(applicationContextMock);
		Mockito.when(applicationContextMock.getEnvironment()).thenReturn(environmentMock);
		Mockito.doAnswer(i -> {
			obtainedFunction = i.getArgument(0, EventLoop.Task.class);
			return null;
		}).when(eventLoop).execute(Mockito.any());
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_duplicatePorts() {
		final HashMap<String, User> userMap = new HashMap<>();
		userMap.put("u1", new User());
		userMap.put("u2", new User());
		Mockito.when(userProviderMock.getUsers()).thenReturn(userMap);
		Mockito.when(environmentMock.resolvePlaceholders(Mockito.any())).thenReturn("d1").thenReturn("d2");
		impl.onApplicationEvent(null);
		obtainedFunction.run();
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
		impl.onApplicationEvent(null);
		obtainedFunction.run();
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
		impl.onApplicationEvent(null);
		obtainedFunction.run();
		Mockito.verify(serverManagerMock).doStartAndMonitorVaultageServers();
	}

}
