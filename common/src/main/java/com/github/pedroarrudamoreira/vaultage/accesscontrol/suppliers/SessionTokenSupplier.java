package com.github.pedroarrudamoreira.vaultage.accesscontrol.suppliers;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpSession;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SessionTokenSupplier implements ITokenSupplier {

	private final SessionController sessionController;
	static final String SESSION_TOKENS = "__SESSION_TOKENS_&*@&";

	private Set<String> getTokensSet() {
		final HttpSession session = sessionController.getCurrentRequest().getSession();
		Set<String> sessionTokens = (Set<String>) session.getAttribute(SESSION_TOKENS);
		if(sessionTokens == null) {
			sessionTokens = new HashSet<>();
			session.setAttribute(SESSION_TOKENS, sessionTokens);
		}
		return sessionTokens;
	}

	@Override
	public String generateNewToken() {
		Set<String> sessionTokens = getTokensSet();
		final String gen = ObjectFactory.generateUUID();
		sessionTokens.add(gen);
		return gen;
	}


	@Override
	public boolean isTokenValid(String token) {
		return getTokensSet().contains(token);
	}

	@Override
	public boolean removeToken(String uuid) {
		if(isTokenValid(uuid)) {
			getTokensSet().remove(uuid);
			return true;
		}
		return false;
	}

}
