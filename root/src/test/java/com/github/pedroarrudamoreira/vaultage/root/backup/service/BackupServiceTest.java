package com.github.pedroarrudamoreira.vaultage.root.backup.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;
import com.github.pedroarrudamoreira.vaultage.root.backup.provider.BackupProvider;
import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import com.github.pedroarrudamoreira.vaultage.root.util.RootObjectFactory;
import com.github.pedroarrudamoreira.vaultage.root.util.zip.EasyZip;
import com.github.pedroarrudamoreira.vaultage.root.vault.sync.VaultSynchronizer;
import com.github.pedroarrudamoreira.vaultage.root.vault.sync.VaultSynchronizer.ExceptionRunnable;
import com.github.pedroarrudamoreira.vaultage.test.util.TestUtils;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;
@RunWith(PowerMockRunner.class)
@PrepareForTest({SessionController.class, ObjectFactory.class, RootObjectFactory.class})
public class BackupServiceTest {
	
	private static final String FAKE_DATA_DIR = "/data-dir";

	private static final String FAKE_BACKUP_PROVIDER = "provider1";

	private static final String FAKE_USER_ID = "user1";

	private static final String FAKE_HOST = "this.server.com";
	
	private static final byte[] FAKE_BYTE_CONTENT = "hello".getBytes();

	@Mock
	private ApplicationContext mockApplicationContext;
	
	@Mock
	private VaultSynchronizer vaultSynchronizer;
	
	@Mock
	private AuthenticationProvider mockAuthProvider;
	
	@Mock
	private BackupProvider mockBackupProvider;
	
	@Mock
	private File mockFile;
	
	@Mock
	private EasyZip easyZipMock;
	
	@Mock
	private ByteArrayOutputStream mockByteArrayOutputStream;
	
	@Mock
	private ByteArrayInputStream mockByteArrayInputStream;
	
	private BackupService backupService;

	
	@BeforeClass
	public static void setupStatic() {
		TestUtils.doPrepareForTest();
	}
	
	@Before
	public void setup() {
		setupStatic();
		backupService = new BackupService();
		backupService.setEnabled(true);
		backupService.setThisServerHost(FAKE_HOST);
		backupService.setAuthProvider(mockAuthProvider);
		backupService.setVaultSynchronizer(vaultSynchronizer);
		backupService.setProviders(Collections.singletonMap(FAKE_BACKUP_PROVIDER, mockBackupProvider));
		PowerMockito.when(SessionController.getApplicationContext()).thenReturn(mockApplicationContext);
		PowerMockito.when(ObjectFactory.buildFile(FAKE_DATA_DIR)).thenReturn(mockFile);
		PowerMockito.when(RootObjectFactory.buildByteArrayOutputStream()).thenReturn(mockByteArrayOutputStream);
		PowerMockito.when(RootObjectFactory.buildByteArrayInputStream(FAKE_BYTE_CONTENT)).thenReturn(
				mockByteArrayInputStream);
		Mockito.when(mockByteArrayOutputStream.toByteArray()).thenReturn(FAKE_BYTE_CONTENT);
		Mockito.when(mockApplicationContext.getBean(BackupService.class)).thenReturn(backupService);
		Mockito.when(vaultSynchronizer.runSync(
				Mockito.any(), Mockito.any())).thenAnswer(
						(i) -> i.getArgument(1, ExceptionRunnable.class).run());
	}
	
	@Test
	public void testNotEnabled() throws JobExecutionException {
		backupService.setEnabled(false);
		new BackupService().execute(null);
		Mockito.verify(mockApplicationContext).getBean(BackupService.class);
		Mockito.verify(mockAuthProvider, Mockito.never()).getUsers();
	}
	
	@Test
	public void testEnabledWithNoUsers() throws JobExecutionException {
		Mockito.when(mockAuthProvider.getUsers()).thenReturn(Collections.emptyMap());
		backupService.execute(null);
		Mockito.verify(mockAuthProvider).getUsers();
	}
	
	@Test
	public void testEnabledWithUsersWithInvalidProviders() throws JobExecutionException {
		User user = new User();
		user.setBackupConfig(Collections.singletonMap("not-valid-provider", new Object()));
		Mockito.when(mockAuthProvider.getUsers()).thenReturn(Collections.singletonMap(FAKE_USER_ID, user));
		backupService.execute(null);
		PowerMockito.verifyStatic(ObjectFactory.class, Mockito.never());
		ObjectFactory.buildFile(Mockito.any());
	}
	
	@Test
	public void testEnabledWithUsersWithValidProvidersAndNonExistingDataDir() throws JobExecutionException {
		User user = new User();
		user.setDataDir(FAKE_DATA_DIR);
		Object providerArg = new Object();
		user.setBackupConfig(Collections.singletonMap(FAKE_BACKUP_PROVIDER, providerArg));
		Mockito.when(mockAuthProvider.getUsers()).thenReturn(Collections.singletonMap(FAKE_USER_ID, user));
		Mockito.when(mockFile.exists()).thenReturn(false);
		backupService.execute(null);
		PowerMockito.verifyStatic(RootObjectFactory.class, Mockito.never());
		RootObjectFactory.buildEasyZip(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
	}
	
	@Test
	public void testEnabledWithUsersWithValidProvidersAndExistingDataDir() throws Exception {
		User user = new User();
		user.setDataDir(FAKE_DATA_DIR);
		Object providerArg = new Object();
		user.setBackupConfig(Collections.singletonMap(FAKE_BACKUP_PROVIDER, providerArg));
		Mockito.when(mockAuthProvider.getUsers()).thenReturn(Collections.singletonMap(FAKE_USER_ID, user));
		Mockito.when(mockFile.exists()).thenReturn(true);
		PowerMockito.when(RootObjectFactory.buildEasyZip(mockFile, FAKE_HOST, false)).thenReturn(easyZipMock);
		backupService.setDoEncrypt(true);
		backupService.execute(null);
		// let event loop do its things...
		Thread.sleep(2000l);
		Mockito.verify(easyZipMock).zipIt(mockByteArrayOutputStream);
		Mockito.verify(mockBackupProvider).doBackup(user, mockByteArrayInputStream, providerArg);
	}
}
