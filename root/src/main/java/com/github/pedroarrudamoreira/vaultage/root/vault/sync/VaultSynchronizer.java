package com.github.pedroarrudamoreira.vaultage.root.vault.sync;

import java.util.Map;

import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;
import com.github.pedroarrudamoreira.vaultage.root.util.RootObjectFactory;

import lombok.Setter;
import lombok.SneakyThrows;

public class VaultSynchronizer {
	
	public static interface ExceptionRunnable<T> {
		T run() throws Exception;
	}
	@Setter
	private AuthenticationProvider authProvider;
	
	@Setter
	private Boolean enabled;
	
	private final Map<String, Object> locks = RootObjectFactory.buildMap(true);
	
	
	public <T> T runSync(ExceptionRunnable<T> command) {
		return runSync(authProvider.getCurrentUserName(), command);
	}
	
	@SneakyThrows
	public <T> T runSync(String user, ExceptionRunnable<T> command) {
		if(!enabled) {
			return command.run();
		}
		
		final Object lock = locks.computeIfAbsent(user, k -> new Object());
		
		synchronized (lock) {
			return command.run();
		}
	}

}
