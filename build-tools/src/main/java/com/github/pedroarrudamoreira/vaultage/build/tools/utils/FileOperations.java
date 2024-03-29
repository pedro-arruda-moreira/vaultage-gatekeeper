package com.github.pedroarrudamoreira.vaultage.build.tools.utils;

import java.io.File;

import org.apache.commons.lang3.SystemUtils;

import com.github.pedroarrudamoreira.vaultage.process.ProcessSpawner;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

public class FileOperations {

	private FileOperations() {
		super();
	}

	public static void copyFiles(String origin, String destination) throws Exception {
		boolean isWindows = SystemUtils.IS_OS_WINDOWS;
		if(isWindows) {
			ProcessSpawner.executeProcessAndWait(
					retVal -> retVal <= 7,
					"robocopy",
					ObjectFactory.normalizePath(origin),
					ObjectFactory.normalizePath(destination),
					"/E"
					);
		} else {
			ProcessSpawner.executeProcessAndWait(
					"cp",
					"-r",
					ObjectFactory.normalizePath(origin),
					ObjectFactory.normalizePath(destination)
					);
		}
	}

	public static boolean destroy(String location) {
		return destroyImpl(new File(location));
	}
	private static boolean destroyImpl(File file) {
		if(file.isDirectory()) {
			File[] children = file.listFiles();
			for(File child : children) {
				if(!destroyImpl(child)) {
					return false;
				}
			}
		}
		if(!file.delete()) {
			return false;
		}
		return true;
	}

}
