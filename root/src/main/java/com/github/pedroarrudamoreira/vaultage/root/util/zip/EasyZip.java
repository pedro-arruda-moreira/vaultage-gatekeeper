package com.github.pedroarrudamoreira.vaultage.root.util.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.pedroarrudamoreira.vaultage.util.IOUtils;

import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;

public class EasyZip {

	private final List<String> fileList = new ArrayList<>();

	private final File folderLocation;

	private final String folderPath;

	private final char[] password;

	public EasyZip(File folderLocation, char[] password) {
		this.folderLocation = folderLocation;
		this.folderPath = this.folderLocation.getAbsolutePath();
		this.password = password;
		generateFileList(folderLocation);
	}

	public Void zipIt(OutputStream out) throws IOException {
		String source = folderLocation.getName();
		ZipOutputStream zos = null;
		try {
			zos = new ZipOutputStream(out, this.password);

			for (String file : this.fileList) {
				FileInputStream in = null;
				ZipParameters zp = new ZipParameters();
				initializeParameters(zp, file, source);
				zos.putNextEntry(zp);
				try {
					in = new FileInputStream(new File(folderLocation, file));
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
		return string.replace('\\', '/');
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
		return file.substring(folderPath.length() + 1);
	}
}