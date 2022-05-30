package com.github.pedroarrudamoreira.vaultage.root.backup.provider.impl;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.github.pedroarrudamoreira.vaultage.process.ProcessSpawner;
import com.github.pedroarrudamoreira.vaultage.root.backup.provider.BackupProvider;
import com.github.pedroarrudamoreira.vaultage.root.backup.provider.util.BackupProviderUtils;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import com.github.pedroarrudamoreira.vaultage.root.util.RootObjectFactory;
import com.github.pedroarrudamoreira.vaultage.util.IOUtils;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

import lombok.Cleanup;
import lombok.SneakyThrows;

public class ExecuteCommandProvider implements BackupProvider {

	@Override
	@SneakyThrows
	public void doBackup(User user, InputStream database, Object params) {
		File tempFolder = null;
		File tempFile = null;
		File theFile = null;
		try {
			tempFile = RootObjectFactory.buildTempFile("backup", ".zip");
			tempFolder = tempFile.getParentFile();
			theFile = ObjectFactory.buildFile(tempFolder, BackupProviderUtils.createFileName(user));
			@Cleanup OutputStream out = ObjectFactory.buildFileOutputStream(theFile);
			IOUtils.copy(database, out);
			out.close();
			List<String> paramsList = BackupProviderUtils.getParamsAsStringList(params);
			for(int i = 0; i < paramsList.size(); i++) {
				paramsList.set(i, String.format(paramsList.get(i), theFile.getAbsolutePath()));
			}
			ProcessSpawner.executeProcessAndWait(paramsList.toArray(new String[paramsList.size()]));
		} finally {
			if(tempFile != null) {
				tempFile.delete();
			}
			if(theFile != null) {
				theFile.delete();
			}
		}
	}

}
