package com.github.pedroarrudamoreira.vaultage.root.backup.provider.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.github.pedroarrudamoreira.vaultage.root.backup.provider.BackupProvider;
import com.github.pedroarrudamoreira.vaultage.root.backup.provider.util.BackupProviderUtils;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import com.github.pedroarrudamoreira.vaultage.util.IOUtils;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

import lombok.Cleanup;
import lombok.SneakyThrows;

public class FileCopyProvider implements BackupProvider {

	@Override
	@SneakyThrows
	public void doBackup(User user, InputStream database, Object params) {
		String fileName = BackupProviderUtils.createFileName(user);
		List<String> targetDirectories = BackupProviderUtils.getParamsAsStringList(params);
		for(String dir : targetDirectories) {
			File f = new File(dir);
			if(f.exists() && !f.isDirectory()) {
				continue;
			}
			f.mkdirs();
			if(!f.exists()) {
				continue;
			}
			@Cleanup OutputStream out = new FileOutputStream(ObjectFactory.buildFile(f, fileName));
			IOUtils.copy(database, out);
		}
	}

}
