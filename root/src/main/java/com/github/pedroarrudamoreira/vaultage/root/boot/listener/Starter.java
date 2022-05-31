package com.github.pedroarrudamoreira.vaultage.root.boot.listener;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import org.springframework.web.context.ServletContextAware;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;
import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import com.github.pedroarrudamoreira.vaultage.root.server.VaultageServerManager;
import com.github.pedroarrudamoreira.vaultage.util.EventLoop;

import lombok.Setter;

public class Starter implements ServletContextAware {
	@Setter
	private AuthenticationProvider userProvider;
	
	@Setter
	private VaultageServerManager serverManager;


	@Override
	public void setServletContext(ServletContext servletContext) {
		doConfigureVaultageServers(servletContext, userProvider.getUsers());
	}
	

	private void doValidateUsers(Map<String, User> users) {
		Set<String> dataDirs = new HashSet<>();
		Set<Integer> ports = new HashSet<Integer>();
		for(Entry<String, User> userEntry : users.entrySet()) {
			User user = userEntry.getValue();
			String userId = userEntry.getKey();
			if(!dataDirs.add(user.getDataDir())) {
				throw new IllegalArgumentException(String.format("duplicate paths (detected on user %s)", userId));
			}
			if(!ports.add(user.getPort())) {
				throw new IllegalArgumentException(String.format("duplicate ports (detected on user %s)", userId));
			}
		}
	}


	private void doConfigureVaultageServers(ServletContext servletContext, Map<String, User> users) {
		EventLoop.repeatTask(() -> {
			if (SessionController.getApplicationContext() == null) {
				return true;
			}
			doValidateUsers(users);
			serverManager.doStartAndMonitorVaultageServers();
			return false;
		}, 500, TimeUnit.MILLISECONDS);
	}

}
