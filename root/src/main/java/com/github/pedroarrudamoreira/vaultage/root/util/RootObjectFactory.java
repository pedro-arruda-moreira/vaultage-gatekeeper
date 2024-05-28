package com.github.pedroarrudamoreira.vaultage.root.util;

import com.github.pedroarrudamoreira.vaultage.root.util.zip.EasyZip;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

import javax.mail.Authenticator;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Deprecated
public class RootObjectFactory {
	private RootObjectFactory() {
		super();
	}
	
	public static Session buildEmailSession(Properties properties, Authenticator authenticator) {
		return ObjectFactory.invokeStatic(Session.class, "getDefaultInstance", properties, authenticator);
	}
	
	public static MimeMessage buildMimeMessage(Session session) {
		return ObjectFactory.build(MimeMessage.class, session);
	}
	
	public static EasyZip buildEasyZip(File folder, String password, boolean hideContents) {
		return ObjectFactory.fromSupplier(EasyZipSupplier.class, folder, password, hideContents);
	}
	
	public static ByteArrayOutputStream buildByteArrayOutputStream() {
		return ObjectFactory.build(ByteArrayOutputStream.class);
	}
	
	public static ByteArrayInputStream buildByteArrayInputStream(byte[] content) {
		return ObjectFactory.build(ByteArrayInputStream.class, (Object) content);
	}
	
	public static OutputStream buildFileOutputStream(File file) throws FileNotFoundException {
		return ObjectFactory.buildFileOutputStream(file);
	}
	
	public static <K, V> Map<K, V> buildMap(boolean concurrent) {
		if(concurrent) {
			return ObjectFactory.build(ConcurrentHashMap.class);
		}
		return ObjectFactory.build(HashMap.class);
	}
	
	public static File buildTempFile(String prefix, String suffix) throws IOException {
		return ObjectFactory.invokeStatic(File.class, "createTempFile", prefix, suffix);
	}

}
