package com.github.pedroarrudamoreira.vaultage.pwa.security;

import java.util.Arrays;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;
import com.github.pedroarrudamoreira.vaultage.accesscontrol.TokenManager;

import lombok.Setter;

public class TokenService implements UserDetailsService {
	public static final String CRYPTO_TYPE = "crypto_type";
	@Setter
	private String cryptoType;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		String userPassword;
		final HttpServletRequest currentRequest = SessionController.getCurrentRequest();
		currentRequest.setAttribute(CRYPTO_TYPE, cryptoType);
		String providedToken = currentRequest.getParameter("value");
		if(TokenManager.isTokenValid(providedToken)) {
			TokenManager.removeToken(providedToken);
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
