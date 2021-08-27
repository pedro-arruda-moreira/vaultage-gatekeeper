package com.github.pedroarrudamoreira.vaultage.root.service.backup;

import java.io.InputStream;

import org.springframework.security.core.userdetails.User;

public interface BackupProvider {
	
	void doBackup(User user, InputStream database, Object params);

}
