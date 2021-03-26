package com.github.pedroarrudamoreira.vaultage.root.service.backup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.util.ByteArrayDataSource;
import javax.servlet.ServletContext;

import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.web.context.ServletContextAware;

import com.github.pedroarrudamoreira.vaultage.root.service.email.EmailService;
import com.github.pedroarrudamoreira.vaultage.root.util.crypto.EasyAES;
import com.github.pedroarrudamoreira.vaultage.root.util.crypto.KeyAndIV;
import com.github.pedroarrudamoreira.vaultage.root.util.zip.EasyZip;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

import lombok.Setter;
import lombok.SneakyThrows;
@Setter
public class BackupService implements DisposableBean, ServletContextAware {

	private static final String TYPE_STARTUP = "startup";
	private String type;

	private boolean enabled;

	private String thisServerHost;

	private EmailService emailService;

	private boolean doEncrypt;

	private static final String FILE_SIGNATURE = "VTGBKP";

	@Override
	public void setServletContext(ServletContext servletContext) {
		if(enabled && type.equals(TYPE_STARTUP)) {
			doBackup();
		}
	}
	@SneakyThrows
	private void doBackup() {
		final File vaultageDataFolder = ObjectFactory.buildFile(new File(SystemUtils.USER_HOME), ".vaultage");
		if(!vaultageDataFolder.exists()) {
			return;
		}
		final ByteArrayOutputStream emailAttachmentData = new ByteArrayOutputStream();
		final EasyZip zipControl = new EasyZip(vaultageDataFolder);
		if(doEncrypt) {
			emailAttachmentData.write(FILE_SIGNATURE.getBytes(StandardCharsets.ISO_8859_1));
			ByteArrayInputStream encryptedZip = doCrypto(zipControl);
			copy(encryptedZip, emailAttachmentData);
		} else {
			zipControl.zipIt(emailAttachmentData);
		}
		final ByteArrayDataSource dataSource = new ByteArrayDataSource(emailAttachmentData.toByteArray(),
				"text/plain");
		dataSource.setName(createFileName());
		emailService.sendEmail("Periodic Vaultage backup", "TODO: improve this email body!",
				dataSource);
	}
	private String createFileName() {
		String fileExtension = ".zip";
		if(doEncrypt) {
			fileExtension = ".vtgb";
		}
		return String.format("vaultage_backup_%s%s",
				new SimpleDateFormat("yyyyMMdd").format(new Date()),
				fileExtension);
	}
	public ByteArrayInputStream doCrypto(final EasyZip zipControl)
			throws NoSuchAlgorithmException, IOException, Exception {
		final ByteArrayOutputStream baosFromZip = new ByteArrayOutputStream();
		KeyAndIV k = KeyAndIV.fromString(thisServerHost);
		zipControl.zipIt(baosFromZip);
		ByteArrayInputStream encryptedZip = new ByteArrayInputStream(
				new EasyAES(k).encrypt(baosFromZip.toByteArray()));
		return encryptedZip;
	}

	private void copy(InputStream encryptedZip,
			OutputStream emailAttachmentData) throws IOException {
		byte[] buffer = new byte[1024];
		int len;
		while((len = encryptedZip.read(buffer)) > -1) {
			emailAttachmentData.write(buffer, 0, len);
		}
	}
	@Override
	public void destroy() throws Exception {
		if(enabled && !type.equals(TYPE_STARTUP)) {
			doBackup();
		}
	}

}
