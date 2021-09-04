package com.github.pedroarrudamoreira.vaultage.accesscontrol;

import org.junit.Assert;
import org.junit.Test;

public class TokenManagerTest {
	
	@Test
	public void testTokenManager() throws Exception {
		String token = TokenManager.generateNewToken(TokenType.GLOBAL);
		Assert.assertTrue(TokenManager.isTokenValid(token, TokenType.GLOBAL));
		Assert.assertFalse(TokenManager.isTokenValid("nopenopenope", TokenType.GLOBAL));
		Assert.assertFalse(TokenManager.isTokenValid(null, TokenType.GLOBAL));
		Assert.assertTrue(TokenManager.removeToken(token));
	}

}
