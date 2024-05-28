package com.github.pedroarrudamoreira.vaultage.root.vault.sync;

import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;
import com.github.pedroarrudamoreira.vaultage.test.util.AbstractTest;
import com.github.pedroarrudamoreira.vaultage.test.util.ObjectFactoryBuilder;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.concurrent.ConcurrentHashMap;
public class VaultSynchronizerTest extends AbstractTest {
	@Mock
	private AuthenticationProvider authProviderMock;

	@Mock
	@ObjectFactoryBuilder
	private ConcurrentHashMap<String, Object> lockMapMock;

	@Mock
	private ObjectFactory objectFactory;
	
	private static final Object LOCK = new Object();
	
	private VaultSynchronizer unit;
	
	@Before
	public void setup() {
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
