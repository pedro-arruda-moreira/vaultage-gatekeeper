package com.github.pedroarrudamoreira.vaultage.root.backup.provider.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import com.github.pedroarrudamoreira.vaultage.root.util.RootObjectFactory;
import com.github.pedroarrudamoreira.vaultage.test.util.TestUtils;
import com.github.pedroarrudamoreira.vaultage.util.IOUtils;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
	ObjectFactory.class,
	LogFactory.class,
	IOUtils.class,
	RootObjectFactory.class
})
public class FileCopyProviderTest {

	private static final String FAKE_PATH = "/path/to/file.txt";

	@Mock
	private OutputStream fileOutputStreamMock;
	
	@Mock
	private Log logMock;
	
	@Mock
	private File fileMock;
	
	private FileCopyProvider impl;
	
	@BeforeClass
	public static void setupStatic() {
		TestUtils.doPrepareForTest();
	}
	
	@Before
	public void setup() throws FileNotFoundException {
		setupStatic();
		PowerMockito.when(LogFactory.getLog(FileCopyProvider.class)).thenReturn(logMock);
		PowerMockito.when(ObjectFactory.buildFile(FAKE_PATH)).thenReturn(fileMock);
		PowerMockito.when(RootObjectFactory.buildFileOutputStream(Mockito.any())).thenReturn(
				fileOutputStreamMock);
		impl = new FileCopyProvider();
	}
	
	@Test
	public void testNoDirectories() {
		User user = new User();
		impl.doBackup(user, null, Collections.emptyList());
		PowerMockito.verifyStatic(ObjectFactory.class, Mockito.never());
		ObjectFactory.buildFile(Mockito.any());
	}
	
	@Test
	public void testFileAlreadyExists() {
		User user = new User();
		Mockito.when(fileMock.exists()).thenReturn(true);
		Mockito.when(fileMock.isDirectory()).thenReturn(false);
		Mockito.doAnswer((i) -> {
			Assert.assertTrue("wrong log content", i.getArgument(0, String.class).contains(
					"because it already exists"));
			return null;
		}).when(logMock).warn(Mockito.any());
		impl.doBackup(user, null, Collections.singletonList(FAKE_PATH));
		Mockito.verify(fileMock, Mockito.never()).mkdirs();
	}
	
	@Test
	public void testCouldNotCreateFile() {
		User user = new User();
		Mockito.when(fileMock.exists()).thenReturn(false).thenReturn(false);
		Mockito.doAnswer((i) -> {
			Assert.assertTrue("wrong log content", i.getArgument(0, String.class).contains(
					"Could not create"));
			return null;
		}).when(logMock).warn(Mockito.any());
		impl.doBackup(user, null, Collections.singletonList(FAKE_PATH));
		Mockito.verify(fileMock).mkdirs();
	}
	
	@Test
	public void testOk() throws IOException {
		User user = new User();
		Mockito.when(fileMock.exists()).thenReturn(false).thenReturn(true);
		ByteArrayInputStream fakeDatabase = new ByteArrayInputStream("hello".getBytes());
		impl.doBackup(user, fakeDatabase, Collections.singletonList(FAKE_PATH));
		Mockito.verify(fileMock).mkdirs();
		PowerMockito.verifyStatic(IOUtils.class);
		IOUtils.copy(fakeDatabase, fileOutputStreamMock);
	}
	
	
	
}
