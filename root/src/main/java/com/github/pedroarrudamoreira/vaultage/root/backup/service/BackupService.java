package com.github.pedroarrudamoreira.vaultage.root.backup.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;
import com.github.pedroarrudamoreira.vaultage.root.backup.provider.BackupProvider;
import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import com.github.pedroarrudamoreira.vaultage.root.util.zip.EasyZip;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

import lombok.Setter;
import lombok.SneakyThrows;
@Setter
public class BackupService implements Job {

	private boolean enabled;

	private String thisServerHost;
	
	private AuthenticationProvider authProvider;

	private boolean doEncrypt;
	
	private Map<String, BackupProvider> providers;

	@SneakyThrows
	private void doBackup() {
		for(User user : authProvider.getUsers().values()) {
			Map<String, Object> backupConfig = user.getBackupConfig();
			if(backupConfig == null || backupConfig.isEmpty()) {
				continue;
			}
			List<Map.Entry<BackupProvider, Object>> providersForUser = findProvidersForUser(backupConfig);
			final File vaultageDataFolder = ObjectFactory.buildFile(user.getDataDir());
			if(!vaultageDataFolder.exists()) {
				continue;
			}
			byte[] vaultageDatabaseBytes = doZipDatabase(vaultageDataFolder);
			for(Map.Entry<BackupProvider, Object> providerConfig : providersForUser) {
				providerConfig.getKey().doBackup(user, new ByteArrayInputStream(vaultageDatabaseBytes),
						providerConfig.getValue());
			}
		}
		
	}
	private byte[] doZipDatabase(final File vaultageDataFolder) throws IOException {
		final ByteArrayOutputStream vaultageDatabase = new ByteArrayOutputStream();
		EasyZip zipControl = null; 
		if(doEncrypt) {
			zipControl = new EasyZip(vaultageDataFolder, thisServerHost.toCharArray());
		} else {
			zipControl = new EasyZip(vaultageDataFolder, null);
		}
		zipControl.zipIt(vaultageDatabase);
		byte[] vaultageDatabaseBytes = vaultageDatabase.toByteArray();
		return vaultageDatabaseBytes;
	}
	private List<Map.Entry<BackupProvider, Object>> findProvidersForUser(Map<String, Object> backupConfig) {
		ArrayList<Map.Entry<BackupProvider, Object>> foundProviders = new ArrayList<>();
		for(String key : backupConfig.keySet()) {
			if(!providers.containsKey(key)) {
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
