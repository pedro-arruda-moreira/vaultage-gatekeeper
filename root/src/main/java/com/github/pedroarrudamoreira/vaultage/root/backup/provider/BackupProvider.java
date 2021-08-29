package com.github.pedroarrudamoreira.vaultage.root.backup.provider;

import java.io.InputStream;

import com.github.pedroarrudamoreira.vaultage.root.security.model.User;

public interface BackupProvider {
	
	void doBackup(User user, InputStream database, Object params);

}
