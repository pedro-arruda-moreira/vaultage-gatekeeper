package com.github.pedroarrudamoreira.vaultage.root.service.backup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.util.ByteArrayDataSource;
import javax.servlet.ServletContext;

import org.apache.commons.lang3.SystemUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.expression.EvaluationContext;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;
import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;
import com.github.pedroarrudamoreira.vaultage.root.service.email.EmailService;
import com.github.pedroarrudamoreira.vaultage.root.util.zip.EasyZip;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

import lombok.Cleanup;
import lombok.Setter;
import lombok.SneakyThrows;
@Setter
public class BackupService implements Job {

	private boolean enabled;

	private String thisServerHost;
	
	private AuthenticationProvider authProvider;

	private boolean doEncrypt;

	@SneakyThrows
	private void doBackup() {
		final File vaultageDataFolder = ObjectFactory.buildFile(new File(SystemUtils.USER_HOME), ".vaultage");
		if(!vaultageDataFolder.exists()) {
			return;
		}
		final ByteArrayOutputStream emailAttachmentData = new ByteArrayOutputStream();
		EasyZip zipControl = null; 
		if(doEncrypt) {
			zipControl = new EasyZip(vaultageDataFolder, thisServerHost.toCharArray());
		} else {
			zipControl = new EasyZip(vaultageDataFolder, null);
		}
		zipControl.zipIt(emailAttachmentData);
		final ByteArrayDataSource dataSource = new ByteArrayDataSource(emailAttachmentData.toByteArray(),
				"text/plain");
		dataSource.setName(createFileName());
//		emailService.sendEmail("Periodic Vaultage backup", getEmailBody(),
//				dataSource);
	}
	private String getEmailBody() throws IOException {
		@Cleanup InputStream str = BackupService.class.getResourceAsStream("backup-email.html");
		@Cleanup ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int len = -1;
		byte[] buff = new byte[1024];
		while((len = str.read(buff)) > 0) {
			baos.write(buff, 0, len);
		}
		return new String(baos.toByteArray(), StandardCharsets.ISO_8859_1);
	}
	private String createFileName() {
		return String.format("vaultage_backup_%s.zip",
				new SimpleDateFormat("yyyyMMdd").format(new Date()));
	}
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		if(authProvider == null) {
			// Let's delegate to the spring bean.
			WebApplicationContextUtils.getWebApplicationContext(
					SessionController.getCurrentContext()).getBean(BackupService.class).execute(context);
			return;
		}
		if(!enabled) {
			return;
		}
		doBackup();
	}

}
