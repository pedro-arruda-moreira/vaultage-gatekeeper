package com.github.pedroarrudamoreira.vaultage.root.security;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;

import lombok.Getter;
import lombok.Setter;

public class AuthenticationProvider implements UserDetailsService, InitializingBean {
    private static final TypeReference<Map<String, User>> USER_TYPE_REF = new TypeReference<Map<String, User>>() {
    };
    @Setter
    private Resource userConfigFile;
    @Getter
    private Map<String, User> users;

    @Setter
    private String implementation;

    public AuthenticationProvider() {
        super();
    }

    public User getCurrentUser() {
        String currentUserName = getCurrentUserName();
        if (currentUserName != null) {
            return users.get(currentUserName);
        }
        return null;
    }

    public String getCurrentUserName() {
        String currentUserName = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            currentUserName = authentication.getName();
        }
        return currentUserName;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = users.get(username);
        if (user == null) {
            throw new UsernameNotFoundException(String.format("User %s not configured.", username));
        }
        return new org.springframework.security.core.userdetails.User(username,
                String.format("{noop}%s", getPassword(user)),
                Collections.emptyList());
    }

    private String getPassword(User user) {
        if (!"userSelector".equals(implementation)) {
            return user.getPassword();
        }
        return "login";
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        users = new ObjectMapper().readValue(userConfigFile.getFile(), USER_TYPE_REF);
        users.forEach((k, v) -> v.setUserId(k));
    }

}
