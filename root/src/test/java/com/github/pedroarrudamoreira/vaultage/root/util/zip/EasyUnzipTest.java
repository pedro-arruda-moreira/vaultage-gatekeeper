package com.github.pedroarrudamoreira.vaultage.root.util.zip;

import java.io.File;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.pedroarrudamoreira.vaultage.build.tools.utils.FileOperations;

import lombok.Cleanup;

public class EasyUnzipTest {
	
	private static File target;
	private static File desired;
	
	@BeforeClass
	public static void getPaths() throws Exception {
		File projectRoot = new File(EasyUnzipTest.class.getClassLoader().getResource(
				"root_marker").toURI()).getParentFile().getParentFile().getParentFile();
		target = new File(projectRoot, "target");
		desired = new File(target, "testUnzipDir");
		if(desired.exists()) {
			FileOperations.destroy(desired.getAbsolutePath());
		}
	}
	
	@Test
	public void testUnzip() throws Exception {
		@Cleanup final InputStream zipStream = EasyUnzipTest.class.getResourceAsStream(
				"testUnzipDir.zip");
		EasyUnzip.extract(target, zipStream);
		Assert.assertTrue(desired.exists());
		Assert.assertTrue(new File(desired, "file1").exists());
		Assert.assertTrue(new File(desired, "folder1/file2").exists());
	}

}
