package com.github.pedroarrudamoreira.vaultage.root.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AuthenticationProvider implements UserDetailsService, InitializingBean, EnvironmentAware {
    private static final TypeReference<Map<String, User>> USER_TYPE_REF = new TypeReference<Map<String, User>>() {
    };
    @Setter
    private Resource userConfigFile;
    @Getter
    private Map<String, User> users;

    @Setter
    private String implementation;

    @Setter
    private Environment environment;

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
        SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
        File file = userConfigFile.getFile();
        @Cleanup FileInputStream fileInputStream = new FileInputStream(file);
        @Cleanup Reader rd = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        @Cleanup BufferedReader brd = new BufferedReader(rd);
        List<String> lines = new ArrayList<>();
        {
            String line;
            while ((line = brd.readLine()) != null) {
                lines.add(spelExpressionParser.parseExpression(line,
                        new TemplateParserContext()).getValue(new StandardEvaluationContext(environment), String.class));
            }
        }
        StringBuilder bld = new StringBuilder();
        for (String line : lines) {
            bld.append(line);
        }
        users = new ObjectMapper().readValue(bld.toString(), USER_TYPE_REF);
        users.forEach((k, v) -> v.setUserId(k));
    }

}
