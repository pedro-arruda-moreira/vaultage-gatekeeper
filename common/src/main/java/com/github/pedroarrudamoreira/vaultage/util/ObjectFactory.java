package com.github.pedroarrudamoreira.vaultage.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ObjectFactory {
	
	public static final String PRESENT = "__present__";
	
	private ObjectFactory() {
		super();
	}
	
	public static Properties buildProperties() {
		return new Properties();
	}
	
	public static AtomicInteger buildAtomicInteger(int value) {
		return new AtomicInteger(value);
	}
	
	public static String generateUUID() {
		return UUID.randomUUID().toString();
	}
	
	public static File buildFile(String path) {
		return buildFile(null, path);
	}
	
	public static File buildFile(File parent, String name) {
		return new File(parent, name);
	}
	
	public static String normalizePath(String path) {
		return buildFile(path).getAbsolutePath();
	}
	
	public static FileOutputStream buildFileOutputStream(File file) throws FileNotFoundException {
		return new FileOutputStream(file);
	}
	
	public static FileInputStream buildFileInputStream(File file) throws FileNotFoundException {
		return new FileInputStream(file);
	}

}
