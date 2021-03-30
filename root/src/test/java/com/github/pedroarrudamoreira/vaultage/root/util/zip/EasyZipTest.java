package com.github.pedroarrudamoreira.vaultage.root.util.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import lombok.Cleanup;

public class EasyZipTest {
	
	private EasyZip impl;
	
	private static File target;
	
	@BeforeClass
	public static void getPaths() throws Exception {
		target = new File(EasyZipTest.class.getResource("testZipDir/file1").toURI()).getParentFile();
	}
	
	@Before
	public void setup() {
		impl = new EasyZip(target, null);
	}
	
	@Test
	public void testZip() throws Exception {
		@Cleanup final ByteArrayOutputStream zipContent = new ByteArrayOutputStream();
		impl.zipIt(zipContent);
		@Cleanup final ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipContent.toByteArray()));
		ZipEntry ze = null;
		while ((ze = zis.getNextEntry()) != null) {
			if(ze.isDirectory()) {
				continue;
			}
			Assert.assertTrue(ze.getName().contains("file1") || ze.getName().contains("file2"));
		}
	}
	

}
