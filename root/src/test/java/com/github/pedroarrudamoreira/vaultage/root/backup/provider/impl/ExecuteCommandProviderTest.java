package com.github.pedroarrudamoreira.vaultage.root.backup.provider.impl;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.function.Supplier;

import com.github.pedroarrudamoreira.vaultage.process.ProcessSpawnerOptions;
import com.github.pedroarrudamoreira.vaultage.util.EventLoop;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.github.pedroarrudamoreira.vaultage.process.ProcessSpawner;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import com.github.pedroarrudamoreira.vaultage.root.util.RootObjectFactory;
import com.github.pedroarrudamoreira.vaultage.test.util.AbstractTest;
import com.github.pedroarrudamoreira.vaultage.util.IOUtils;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

import lombok.SneakyThrows;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RootObjectFactory.class, ObjectFactory.class, ProcessSpawner.class, IOUtils.class})
public class ExecuteCommandProviderTest {
	
	@Mock
	private File tempFileMock;

	@Mock
	private File tempFolderMock;

	@Mock
	private File theFileMock;
	
	@Mock
	private OutputStream outMock;
	
	@Mock
	private InputStream databaseMock;
	
	@Mock
	private Process processMock;

	@Mock
	private EventLoop eventLoop;
	
	private ExecuteCommandProvider impl;
	
	@BeforeClass
	public static void setupStatic() {
		AbstractTest.prepareMockStatic();
	}
	
	@Before
	@SneakyThrows
	public void setup() {
		setupStatic();
		PowerMockito.when(RootObjectFactory.buildTempFile("backup", ".zip")).thenReturn(tempFileMock);
		Mockito.when(tempFileMock.getParentFile()).thenReturn(tempFolderMock);
		PowerMockito.when(ObjectFactory.buildFile(Mockito.eq(tempFolderMock), Mockito.anyString())).thenReturn(theFileMock);
		PowerMockito.when(ObjectFactory.buildFileOutputStream(theFileMock)).thenReturn(outMock);
		impl = new ExecuteCommandProvider(eventLoop);
		Mockito.doAnswer((i) -> {
			Supplier<Boolean> sup = i.getArgument(0, Supplier.class);
			while (sup.get()) {
				// wait...
			}
			return null;
		}).when(eventLoop).repeatTask(Mockito.any(), Mockito.anyLong(), Mockito.any());
	}
	
	@Test
	@SneakyThrows
	public void test_Executes() {
		Mockito.when(processMock.isAlive()).thenReturn(true).thenReturn(false);
		Mockito.when(theFileMock.getAbsolutePath()).thenReturn("path");
		final ProcessSpawnerOptions[] obtainedProcessArgument = new ProcessSpawnerOptions[1];
		Mockito.when(ProcessSpawner.executeProcess(Mockito.any())).thenAnswer((i) -> {
			obtainedProcessArgument[0] = i.getArgument(0, ProcessSpawnerOptions.class);
			return processMock;
		});
		final User user = new User();
		user.setUserId("usr1");
		impl.doBackup(user, databaseMock, Arrays.asList("test", "%s"));
		Mockito.verify(theFileMock).delete();
		Mockito.verify(tempFileMock).delete();
		Mockito.verify(outMock).close();
		PowerMockito.verifyStatic(IOUtils.class);
		IOUtils.copy(databaseMock, outMock);
		Assert.assertEquals("test", obtainedProcessArgument[0].getCommand()[0]);
	}

}
