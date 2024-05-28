package com.github.pedroarrudamoreira.vaultage.root.boot.listener;

import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import com.github.pedroarrudamoreira.vaultage.root.server.VaultageServerManager;
import com.github.pedroarrudamoreira.vaultage.util.EventLoop;
import lombok.Setter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Starter implements ApplicationListener<ContextRefreshedEvent> {
	@Setter
	private AuthenticationProvider userProvider;
	
	@Setter
	private VaultageServerManager serverManager;

	@Setter
	private EventLoop eventLoop;

	private void doValidateUsers(Map<String, User> users) {
		Set<String> dataDirs = new HashSet<>();
		Set<Integer> ports = new HashSet<>();
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

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		eventLoop.execute(() -> {
			doValidateUsers(userProvider.getUsers());
			serverManager.doStartAndMonitorVaultageServers();
		});
	}
}
