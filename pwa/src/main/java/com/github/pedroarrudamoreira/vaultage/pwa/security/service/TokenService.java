package com.github.pedroarrudamoreira.vaultage.pwa.security.service;

import java.util.Arrays;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;
import com.github.pedroarrudamoreira.vaultage.accesscontrol.TokenManager;
import com.github.pedroarrudamoreira.vaultage.accesscontrol.TokenType;

import lombok.Setter;

public class TokenService implements UserDetailsService {
	public static final String CRYPTO_TYPE = "crypto_type";
	public static final String USE_BASIC = "use_basic";
	public static final String CONFIG_CACHE = "config_cache";
	public static final String AUTO_CREATE = "auto_create";
	public static final String OFFLINE_ENABLED = "offline_enabled";
	@Setter
	private String cryptoType;
	@Setter
	private Boolean twoFactorAuth;
	@Setter
	private String autoCreate;
	@Setter
	private String configCache;
	@Setter
	private String offlineEnabled;
	@Setter
	private TokenManager tokenManager;
	@Setter
	@Autowired
	private SessionController sessionController;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		String userPassword;
		final HttpServletRequest currentRequest = sessionController.getCurrentRequest();
		currentRequest.setAttribute(AUTO_CREATE, autoCreate);
		currentRequest.setAttribute(CONFIG_CACHE, configCache);
		currentRequest.setAttribute(CRYPTO_TYPE, cryptoType);
		currentRequest.setAttribute(OFFLINE_ENABLED, offlineEnabled);
		currentRequest.setAttribute(USE_BASIC, Boolean.toString(!twoFactorAuth));
		String providedToken = currentRequest.getParameter("value");
		if(tokenManager.isTokenValid(providedToken, TokenType.GLOBAL)) {
			tokenManager.removeToken(providedToken);
			userPassword = providedToken;
		} else {
			userPassword = UUID.randomUUID().toString();
		}
		userPassword = ("token".equals(username) ? userPassword : UUID.randomUUID(
				).toString());
		return new User(
				username, "{noop}" + userPassword, true, true, true, true,
				Arrays.asList(new SimpleGrantedAuthority("role1")));
	}

}
