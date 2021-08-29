package com.github.pedroarrudamoreira.vaultage.root.backup.provider.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.mail.util.ByteArrayDataSource;

import com.github.pedroarrudamoreira.vaultage.root.backup.provider.BackupProvider;
import com.github.pedroarrudamoreira.vaultage.root.backup.provider.util.BackupProviderUtils;
import com.github.pedroarrudamoreira.vaultage.root.email.service.EmailService;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import com.github.pedroarrudamoreira.vaultage.util.IOUtils;

import lombok.Cleanup;
import lombok.Setter;
import lombok.SneakyThrows;

public class EmailAttachmentProvider implements BackupProvider {
	@Setter
	private EmailService emailService;
	
	@Override
	@SneakyThrows
	public void doBackup(User user, InputStream database, Object params) {
		
		final ByteArrayDataSource dataSource = new ByteArrayDataSource(database, "text/plain");
		dataSource.setName(BackupProviderUtils.createFileName(user));
		emailService.sendEmail(BackupProviderUtils.getParamsAsString(params), "Periodic Vaultage backup", getEmailBody(),
				dataSource);
	}
	private String getEmailBody() throws IOException {
		@Cleanup InputStream str = EmailAttachmentProvider.class.getResourceAsStream("backup-email.html");
		@Cleanup ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtils.copy(str, baos);
		return new String(baos.toByteArray(), StandardCharsets.ISO_8859_1);
	}

}
