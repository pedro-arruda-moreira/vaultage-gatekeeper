package com.github.pedroarrudamoreira.vaultage.root.backup.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;
import com.github.pedroarrudamoreira.vaultage.root.backup.provider.BackupProvider;
import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import com.github.pedroarrudamoreira.vaultage.root.util.RootObjectFactory;
import com.github.pedroarrudamoreira.vaultage.root.util.zip.EasyZip;
import com.github.pedroarrudamoreira.vaultage.root.vault.sync.VaultSynchronizer;
import com.github.pedroarrudamoreira.vaultage.util.EventLoop;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;

@Setter
@CommonsLog
public class BackupService implements Job {

	private boolean enabled;

	private String thisServerHost;
	
	private AuthenticationProvider authProvider;

	private boolean doEncrypt;
	
	private boolean hideContents;
	
	private Map<String, BackupProvider> providers;
	
	@Setter
	private VaultSynchronizer vaultSynchronizer;

	@Setter @Autowired
	private EventLoop eventLoop;
	@SneakyThrows
	private void doBackup() {
		int timeout = 500;
		for(Entry<String, User> userEntry : authProvider.getUsers().entrySet()) {
			User user = userEntry.getValue();
			Map<String, Object> backupConfig = user.getBackupConfig();
			String userId = userEntry.getKey();
			if(backupConfig == null || backupConfig.isEmpty()) {
				log.info(String.format("backup config is empty for user %s.", userId));
				continue;
			}
			List<Map.Entry<BackupProvider, Object>> providersForUser = findProvidersForUser(backupConfig, userId);
			if(providersForUser.isEmpty()) {
				log.warn(String.format("no providers found for user %s.", userId));
				continue;
			}
			final File vaultageDataFolder = ObjectFactory.buildFile(user.getDataDir());
			if(!vaultageDataFolder.exists()) {
				continue;
			}
			final byte[] vaultageDatabaseBytes = doZipDatabase(vaultageDataFolder, user.getUserId());
			for(Map.Entry<BackupProvider, Object> providerConfig : providersForUser) {
				eventLoop.schedule(() -> {
					providerConfig.getKey().doBackup(user, RootObjectFactory.buildByteArrayInputStream(vaultageDatabaseBytes),
							providerConfig.getValue());
				}, timeout, TimeUnit.MILLISECONDS);
				timeout += 500;
			}
		}
		
	}
	private byte[] doZipDatabase(final File vaultageDataFolder, String userId) throws IOException {
		final ByteArrayOutputStream vaultageDatabase = RootObjectFactory.buildByteArrayOutputStream();
		EasyZip zipControl = null; 
		if(doEncrypt) {
			zipControl = RootObjectFactory.buildEasyZip(vaultageDataFolder, thisServerHost, hideContents);
		} else {
			zipControl = RootObjectFactory.buildEasyZip(vaultageDataFolder, null, false);
		}
		final EasyZip zc = zipControl;
		vaultSynchronizer.runSync(userId, () -> zc.zipIt(vaultageDatabase));
		return vaultageDatabase.toByteArray();
	}
	private List<Map.Entry<BackupProvider, Object>> findProvidersForUser(Map<String, Object> backupConfig,
			String userId) {
		ArrayList<Map.Entry<BackupProvider, Object>> foundProviders = new ArrayList<>();
		for(String key : backupConfig.keySet()) {
			if(!providers.containsKey(key)) {
				log.warn(String.format("Provider %s not found for user %s.", key, userId));
				continue;
			}
			BackupProvider provider = providers.get(key);
			foundProviders.add(Collections.singletonMap(provider, backupConfig.get(key)).entrySet().iterator().next());
		}
		return foundProviders;
	}
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		if(authProvider == null) {
			// Let's delegate to the spring bean.
			SessionController.getApplicationContext().getBean(BackupService.class).execute(context);
			return;
		}
		if(!enabled) {
			return;
		}
		doBackup();
	}

}
