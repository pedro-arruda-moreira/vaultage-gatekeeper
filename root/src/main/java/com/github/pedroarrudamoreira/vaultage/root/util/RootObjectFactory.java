package com.github.pedroarrudamoreira.vaultage.root.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.mail.Authenticator;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import com.github.pedroarrudamoreira.vaultage.root.util.zip.EasyZip;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

public class RootObjectFactory {
	private RootObjectFactory() {
		super();
	}
	
	public static Session buildEmailSession(Properties properties, Authenticator authenticator) {
		return Session.getDefaultInstance(properties, authenticator);
	}
	
	public static MimeMessage buildMimeMessage(Session session) {
		return new MimeMessage(session);
	}
	
	public static EasyZip buildEasyZip(File folder, String password, boolean hideContents) {
		char[] charArray = null;
		if(password != null) {
			charArray = password.toCharArray();
		}
		return new EasyZip(folder, charArray, hideContents);
	}
	
	public static ByteArrayOutputStream buildByteArrayOutputStream() {
		return new ByteArrayOutputStream();
	}
	
	public static ByteArrayInputStream buildByteArrayInputStream(byte[] content) {
		return new ByteArrayInputStream(content);
	}
	
	public static OutputStream buildFileOutputStream(File file) throws FileNotFoundException {
		return ObjectFactory.buildFileOutputStream(file);
	}
	
	public static <K, V> Map<K, V> buildMap(boolean concurrent) {
		if(concurrent) {
			return new ConcurrentHashMap<>();
		}
		return new HashMap<>();
	}
	
	public static File buildTempFile(String prefix, String suffix) throws IOException {
		return File.createTempFile(prefix, suffix);
	}

}
