package com.github.pedroarrudamoreira.vaultage.root.listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.springframework.web.context.ServletContextAware;

import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;

import lombok.Setter;

public class Starter implements ServletContextAware {
	@Setter
	private AuthenticationProvider userProvider;
	

	@Override
	public void setServletContext(ServletContext servletContext) {
		ServletRegistration servletRegistration = servletContext.getServletRegistration("proxyServlet");
		userProvider.getUsers().values().forEach((user) -> {
			servletRegistration.addMapping(String.format("/%s/*", user.getVaultageUsername()));
		});
	}

}
