package com.github.pedroarrudamoreira.vaultage.root.backup.provider.impl;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.github.pedroarrudamoreira.vaultage.root.backup.provider.BackupProvider;
import com.github.pedroarrudamoreira.vaultage.root.backup.provider.util.BackupProviderUtils;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import com.github.pedroarrudamoreira.vaultage.root.util.RootObjectFactory;
import com.github.pedroarrudamoreira.vaultage.util.IOUtils;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.apachecommons.CommonsLog;
@CommonsLog
public class FileCopyProvider implements BackupProvider {

	@Override
	@SneakyThrows
	public void doBackup(User user, InputStream database, Object params) {
		String fileName = BackupProviderUtils.createFileName(user);
		List<String> targetDirectories = BackupProviderUtils.getParamsAsStringList(params);
		for(String dir : targetDirectories) {
			File f = ObjectFactory.buildFile(dir);
			if(f.exists() && !f.isDirectory()) {
				log.warn(String.format("Skipping [%s] because it already exists (and is not a file)", dir));
				continue;
			}
			f.mkdirs();
			if(!f.exists()) {
				log.warn(String.format("Could not create [%s]", dir));
				continue;
			}
			@Cleanup OutputStream out = RootObjectFactory.buildFileOutputStream(ObjectFactory.buildFile(f, fileName));
			IOUtils.copy(database, out);
		}
	}

}
