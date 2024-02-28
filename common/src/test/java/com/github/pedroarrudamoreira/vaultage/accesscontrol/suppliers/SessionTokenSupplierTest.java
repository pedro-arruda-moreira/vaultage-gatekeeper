package com.github.pedroarrudamoreira.vaultage.accesscontrol.suppliers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
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

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;
import com.github.pedroarrudamoreira.vaultage.test.util.TestUtils;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ObjectFactory.class})
public class SessionTokenSupplierTest {
	
	@Mock
	private HttpServletRequest mockRequest;
	
	@Mock
	private HttpSession mockSession;
	
	private Set<String> tokens;
	
	private static final String MOCK_TOKEN = UUID.randomUUID().toString();
	
	private SessionTokenSupplier impl;

	@Mock
	private SessionController sessionController;

	@BeforeClass
	public static void setupStatic() {
		TestUtils.prepareMockStatic();
	}
	
	@Before
	public void setup() {
		setupStatic();
		tokens = new HashSet<>();
		impl = new SessionTokenSupplier(sessionController);
		Mockito.when(sessionController.getCurrentRequest()).thenReturn(mockRequest);
		PowerMockito.when(ObjectFactory.generateUUID()).thenReturn(MOCK_TOKEN);
		Mockito.when(mockRequest.getSession()).thenReturn(mockSession);
		Mockito.when(mockSession.getAttribute(
				SessionTokenSupplier.SESSION_TOKENS)).thenAnswer(inv -> tokens);
	}
	
	@Test
	public void testInvalidToken() {
		Assert.assertFalse(impl.isTokenValid("nopenope"));
	}
	
	@Test
	public void testValidToken() {
		tokens.add(MOCK_TOKEN);
		Assert.assertTrue(impl.isTokenValid(MOCK_TOKEN));
	}
	
	@Test
	public void testNullSet() {
		tokens = null;
		Assert.assertFalse(impl.isTokenValid(MOCK_TOKEN));
	}
	
	@Test
	public void testGenerateToken() throws Exception {
		Assert.assertEquals(MOCK_TOKEN, impl.generateNewToken());
		Assert.assertEquals(1, tokens.size());
		Assert.assertEquals(MOCK_TOKEN, tokens.toArray()[0]);
	}
	
	@Test
	public void testDeleteNonExistingToken() throws Exception {
		Assert.assertFalse(impl.removeToken(MOCK_TOKEN));
	}
	
	@Test
	public void testDeleteExistingToken() throws Exception {
		tokens.add(MOCK_TOKEN);
		Assert.assertTrue(impl.removeToken(MOCK_TOKEN));
	}
	
	
}
