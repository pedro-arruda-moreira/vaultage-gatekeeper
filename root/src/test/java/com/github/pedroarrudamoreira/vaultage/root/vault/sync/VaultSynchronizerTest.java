package com.github.pedroarrudamoreira.vaultage.root.vault.sync;

import java.util.Map;

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

import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;
import com.github.pedroarrudamoreira.vaultage.root.util.RootObjectFactory;
import com.github.pedroarrudamoreira.vaultage.test.util.TestUtils;
@RunWith(PowerMockRunner.class)
@PrepareForTest({RootObjectFactory.class})
public class VaultSynchronizerTest {
	@Mock
	private AuthenticationProvider authProviderMock;

	@Mock
	private Map<String, Object> lockMapMock;
	
	private static final Object LOCK = new Object();
	
	private VaultSynchronizer unit;
	
	@BeforeClass
	public static void setupStatic() {
		TestUtils.prepareMockStatic();
	}
	
	@Before
	public void setup() {
		setupStatic();
		PowerMockito.when(RootObjectFactory.buildMap(true)).thenReturn((Map)lockMapMock);
		unit = new VaultSynchronizer();
		unit.setAuthProvider(authProviderMock);
	}
	
	@Test
	public void testNotEnabled() {
		unit.setEnabled(false);
		Object ret = unit.runSync("test", () -> {
			Assert.assertFalse(Thread.holdsLock(LOCK));
			return unit;
		});
		Assert.assertEquals(unit, ret);
		Mockito.verify(lockMapMock, Mockito.never()).computeIfAbsent(Mockito.any(), Mockito.any());
	}
	
	@Test
	public void testEnabledByUser() {
		unit.setEnabled(true);
		Mockito.when(lockMapMock.computeIfAbsent(Mockito.eq("test"), Mockito.any())).thenReturn(LOCK);
		Object ret = unit.runSync("test", () -> {
			Assert.assertTrue(Thread.holdsLock(LOCK));
			return unit;
		});
		Assert.assertEquals(unit, ret);
		Assert.assertFalse(Thread.holdsLock(LOCK));
	}
	
	@Test
	public void testEnabledByCurrentUser() {
		unit.setEnabled(true);
		Mockito.when(authProviderMock.getCurrentUserName()).thenReturn("test");
		Mockito.when(lockMapMock.computeIfAbsent(Mockito.eq("test"), Mockito.any())).thenReturn(LOCK);
		Object ret = unit.runSync(() -> {
			Assert.assertTrue(Thread.holdsLock(LOCK));
			return unit;
		});
		Assert.assertEquals(unit, ret);
		Assert.assertFalse(Thread.holdsLock(LOCK));
	}
}
