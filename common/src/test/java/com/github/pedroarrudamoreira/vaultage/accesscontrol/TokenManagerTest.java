package com.github.pedroarrudamoreira.vaultage.accesscontrol;

import org.junit.Assert;
import org.junit.Test;

public class TokenManagerTest {
	
	@Test
	public void testTokenManager() throws Exception {
		String token = TokenManager.generateNewToken();
		Assert.assertTrue(TokenManager.isTokenValid(token));
		Assert.assertFalse(TokenManager.isTokenValid("nopenopenope"));
		Assert.assertFalse(TokenManager.isTokenValid(null));
		Assert.assertTrue(TokenManager.removeToken(token));
		Assert.assertFalse(TokenManager.removeToken("nopenopenope"));
	}

}
