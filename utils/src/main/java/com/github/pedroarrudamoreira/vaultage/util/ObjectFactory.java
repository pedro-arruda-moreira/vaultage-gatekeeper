package com.github.pedroarrudamoreira.vaultage.util;

import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ObjectFactory {
	
	public static final String PRESENT = "__present__";
	
	public ObjectFactory() {
		super();
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
	
	public static OutputStream buildFileOutputStream(File file) throws FileNotFoundException {
		return new FileOutputStream(file);
	}
	
	public static InputStream buildFileInputStream(File file) throws FileNotFoundException {
		return new FileInputStream(file);
	}

	@SneakyThrows
	public <T> T build(Class<T> clazz, Object ... args) {
		if(args == null || args.length == 0) {
			return clazz.getConstructor().newInstance();
		}
		return clazz.getConstructor(types(args)).newInstance(args);
	}

	private Class<?>[] types(Object[] args) {
		Class<?>[] classes = new Class[args.length];
		int i = 0;
		for(Object o : args) {
			classes[i++] = o.getClass();
		}
		return classes;
	}

}
