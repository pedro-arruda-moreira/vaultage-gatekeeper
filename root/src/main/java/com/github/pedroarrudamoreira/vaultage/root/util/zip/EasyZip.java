package com.github.pedroarrudamoreira.vaultage.root.util.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.pedroarrudamoreira.vaultage.util.IOUtils;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

import lombok.Cleanup;
import lombok.SneakyThrows;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;

public class EasyZip {

	private final List<String> fileList = new ArrayList<>();

	private final File folderLocation;

	private final String folderPath;

	private final char[] password;

	private final EasyZip delegate;
	@SneakyThrows
	public EasyZip(File folderLocation, char[] password) {
		this.password = password;
		if(this.password == null) {
			this.folderLocation = folderLocation;
			delegate = null;
		} else {
			File tempFile = File.createTempFile("data_", ".zip");
			this.folderLocation = tempFile;
			delegate = new EasyZip(folderLocation, null);
		}
		this.folderPath = this.folderLocation.getAbsolutePath();
		if(this.password == null) {
			generateFileList(folderLocation);
		}
	}

	public Void zipIt(OutputStream out) throws IOException {
		if(this.delegate != null) {
			@Cleanup FileOutputStream tempFileOut = ObjectFactory.buildFileOutputStream(this.folderLocation);
			delegate.zipIt(tempFileOut);
			this.generateFileList(this.folderLocation);
		}
		String source = folderLocation.getName();
		ZipOutputStream zos = null;
		try {
			zos = new ZipOutputStream(out, this.password);

			for (String file : this.fileList) {
				FileInputStream in = null;
				ZipParameters zp = new ZipParameters();
				try {
					initializeParameters(zp, file, StringUtils.EMPTY);
					File target = folderLocation;
					if(!target.isFile()) {
						initializeParameters(zp, file, source);
						target = ObjectFactory.buildFile(folderLocation, file);
					}
					in = ObjectFactory.buildFileInputStream(target);
					zos.putNextEntry(zp);
					IOUtils.copy(in, zos);
				} finally {
					in.close();
				}
				zos.closeEntry();
			}


		} catch (IOException ex) {
			throw ex;
		} finally {
			try {
				zos.close();
			} catch (IOException e) {
				// ignored
			}
			if(this.delegate != null) {
				this.folderLocation.delete();
			}
		}
		return null;
	}

	private void initializeParameters(ZipParameters zp, String file, String source) {
		if(this.password != null) {
			zp.setEncryptFiles(true);
			zp.setEncryptionMethod(EncryptionMethod.AES);
			zp.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
		}
		zp.setFileNameInZip(normalize(source + '/' + file));
	}

	private String normalize(String string) {
		String withCorrectSlashes = string.replace('\\', '/');
		if(withCorrectSlashes.startsWith("/")) {
			return withCorrectSlashes.substring(1);
		}
		return withCorrectSlashes;
	}

	private void generateFileList(File file) {

		if (file.isDirectory()) {
			File[] children = file.listFiles();
			for (File child: children) {
				generateFileList(child);
			}
		} else {
			fileList.add(generateFilePath(file.toString()));
		}
	}

	private String generateFilePath(String file) {
		if(folderPath.equals(file)) {
			return file.substring(file.lastIndexOf(File.separator) + 1);
		}
		return file.substring(folderPath.length() + 1);
	}
}