package com.github.pedroarrudamoreira.vaultage.pwa.security;

import java.util.Arrays;
import java.util.UUID;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.TokenManager;
import com.github.pedroarrudamoreira.vaultage.pwa.security.listener.SecurityListener;

public class TokenService implements UserDetailsService {

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		String userPassword;
		String providedToken = SecurityListener.getToken();
		if(TokenManager.isTokenValid(providedToken)) {
			userPassword = providedToken;
		} else {
			userPassword = UUID.randomUUID().toString();
		}
		return new User(
				("token".equals(username) ? username : UUID.randomUUID().toString()),
				"{noop}" + userPassword, true, true, true, true,
				Arrays.asList(new SimpleGrantedAuthority("role1")));
	}

}
