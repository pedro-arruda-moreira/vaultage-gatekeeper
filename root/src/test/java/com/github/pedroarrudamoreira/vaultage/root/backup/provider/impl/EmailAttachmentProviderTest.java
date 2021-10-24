package com.github.pedroarrudamoreira.vaultage.root.backup.provider.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.github.pedroarrudamoreira.vaultage.root.email.service.EmailService;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import com.github.pedroarrudamoreira.vaultage.util.IOUtils;

@RunWith(MockitoJUnitRunner.class)
public class EmailAttachmentProviderTest {

	private static final String FAKE_DATABASE = "hello";

	@Mock
	private EmailService emailServiceMock;

	@InjectMocks
	private EmailAttachmentProvider impl;

	@Test
	public void testSendEmail() throws AddressException, MessagingException {
		User user = new User();
		ByteArrayInputStream bais = new ByteArrayInputStream(FAKE_DATABASE.getBytes());
		Mockito.doAnswer((i) -> {
			Assert.assertTrue("wrong email content", i.getArgument(2, String.class).contains("It may be encrypted in AES-256"));
			InputStream obtainedBais = i.getArgument(3, DataSource.class).getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copy(obtainedBais, baos);
			Assert.assertEquals(FAKE_DATABASE, new String(baos.toByteArray()));
			return null;
		}).when(emailServiceMock).sendEmail(Mockito.eq("email1@test.com,email2@test.com"),
				Mockito.eq("Periodic Vaultage backup"), Mockito.anyString(), Mockito.any());
		impl.doBackup(user, bais, Arrays.asList("email1@test.com", "email2@test.com"));
	}

}
