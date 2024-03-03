package com.github.pedroarrudamoreira.vaultage.root.backup.service;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;
import com.github.pedroarrudamoreira.vaultage.root.backup.provider.BackupProvider;
import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import com.github.pedroarrudamoreira.vaultage.root.util.EasyZipSupplier;
import com.github.pedroarrudamoreira.vaultage.root.util.zip.EasyZip;
import com.github.pedroarrudamoreira.vaultage.root.vault.sync.VaultSynchronizer;
import com.github.pedroarrudamoreira.vaultage.root.vault.sync.VaultSynchronizer.ExceptionRunnable;
import com.github.pedroarrudamoreira.vaultage.test.util.AbstractTest;
import com.github.pedroarrudamoreira.vaultage.test.util.ObjectFactoryBuilder;
import com.github.pedroarrudamoreira.vaultage.test.util.ObjectFactorySupplier;
import com.github.pedroarrudamoreira.vaultage.util.EventLoop;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;
@RunWith(PowerMockRunner.class)
@PrepareForTest({SessionController.class})
public class BackupServiceTest extends AbstractTest {
	
	private static final String FAKE_DATA_DIR = "/data-dir";

	private static final String FAKE_BACKUP_PROVIDER = "provider1";

	private static final String FAKE_USER_ID = "user1";

	private static final String FAKE_HOST = "this.server.com";
	
	public static final byte[] FAKE_BYTE_CONTENT = "hello".getBytes();

	@Mock
	private ApplicationContext mockApplicationContext;
	
	@Mock
	private VaultSynchronizer vaultSynchronizer;
	
	@Mock
	private AuthenticationProvider mockAuthProvider;
	
	@Mock
	private BackupProvider mockBackupProvider;
	
	@Mock
	@ObjectFactoryBuilder(types = {String.class})
	public File mockFile;
	
	@Mock
	@ObjectFactorySupplier(
			clazz = EasyZipSupplier.class,
			args = {
					"#{mockFile}",
					FAKE_HOST,
					"#{false}"
			}
	)
	private EasyZip easyZipMock;
	
	@Mock
	@ObjectFactoryBuilder
	private ByteArrayOutputStream mockByteArrayOutputStream;
	
	@Mock
	@ObjectFactoryBuilder(
			types = byte[].class,
			values = "#{FAKE_BYTE_CONTENT}"
	)
	private ByteArrayInputStream mockByteArrayInputStream;

	@Mock
	private ObjectFactory objectFactory;

	@Mock
	private EventLoop eventLoop;
	
	private BackupService backupService;

	
	@BeforeClass
	public static void setupStatic() {
		AbstractTest.prepareMockStatic();
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
		backupService.setEventLoop(eventLoop);
		PowerMockito.when(SessionController.getApplicationContext()).thenReturn(mockApplicationContext);
		Mockito.when(eventLoop.schedule(Mockito.any(), Mockito.anyLong(), Mockito.any())).thenAnswer(i -> {
			i.getArgument(0, EventLoop.Task.class).run();
			return null;
		});
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
		Mockito.verify(objectFactory, Mockito.never()).doBuild(Mockito.eq(File.class), Mockito.any());
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
		Mockito.verify(objectFactory, Mockito.never()).doBuild(Mockito.eq(EasyZip.class), Mockito.any(), Mockito.any(), Mockito.anyBoolean());
	}
	
	@Test
	public void testEnabledWithUsersWithValidProvidersAndExistingDataDir() throws Exception {
		User user = new User();
		user.setDataDir(FAKE_DATA_DIR);
		Object providerArg = new Object();
		user.setBackupConfig(Collections.singletonMap(FAKE_BACKUP_PROVIDER, providerArg));
		Mockito.when(mockAuthProvider.getUsers()).thenReturn(Collections.singletonMap(FAKE_USER_ID, user));
		Mockito.when(mockFile.exists()).thenReturn(true);
		Mockito.when(objectFactory.doBuild(EasyZip.class, mockFile, FAKE_HOST, false)).thenReturn(easyZipMock);
		backupService.setDoEncrypt(true);
		backupService.execute(null);
		// let event loop do its things...
//		Thread.sleep(2000l);
		Mockito.verify(easyZipMock).zipIt(mockByteArrayOutputStream);
		Mockito.verify(mockBackupProvider).doBackup(user, mockByteArrayInputStream, providerArg);
	}
}
