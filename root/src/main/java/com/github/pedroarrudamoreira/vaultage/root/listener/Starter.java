package com.github.pedroarrudamoreira.vaultage.root.listener;

import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.springframework.web.context.ServletContextAware;

import lombok.Setter;

public class Starter implements ServletContextAware {
	@Setter
	private String users;
	@Setter
	private String port;

	@Override
	public void setServletContext(ServletContext servletContext) {
		ServletRegistration servletRegistration = servletContext.getServletRegistration("vaultage");
		StringTokenizer usersTokenizer = new StringTokenizer(this.users);
		servletRegistration.setInitParameter("targetUri",
				String.format("http://localhost:%s/", port));
		while(usersTokenizer.hasMoreElements()) {
			Object user = usersTokenizer.nextElement();
			servletRegistration.addMapping(String.format("/%s/*", user));
		}
	}

}
