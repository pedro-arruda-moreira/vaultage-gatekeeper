package com.github.pedroarrudamoreira.vaultage.accesscontrol;

import com.github.pedroarrudamoreira.vaultage.util.EventLoop;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TokenManagerTest {

	@Mock
	private EventLoop eventLoop;

	@Mock
	private SessionController sessionController;
	
	@Test
	public void testTokenManager() throws Exception {
		final TokenManager tokenManager = new TokenManager(eventLoop, sessionController);
		String token = tokenManager.generateNewToken(TokenType.GLOBAL);
		Assert.assertTrue(tokenManager.isTokenValid(token, TokenType.GLOBAL));
		Assert.assertFalse(tokenManager.isTokenValid("nopenopenope", TokenType.GLOBAL));
		Assert.assertFalse(tokenManager.isTokenValid(null, TokenType.GLOBAL));
		Assert.assertTrue(tokenManager.removeToken(token));
	}

}
