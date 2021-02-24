package com.github.pedroarrudamoreira.vaultage.pwa.security;

import java.util.Arrays;
import java.util.UUID;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.TokenManager;
import com.github.pedroarrudamoreira.vaultage.listener.RequestHolderListener;

public class TokenService implements UserDetailsService {

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		String userPassword;
		String providedToken = RequestHolderListener.getCurrentRequest().getParameter("value");
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
