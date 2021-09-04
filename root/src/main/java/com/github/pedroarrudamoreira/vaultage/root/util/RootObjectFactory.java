package com.github.pedroarrudamoreira.vaultage.root.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import com.github.pedroarrudamoreira.vaultage.root.util.zip.EasyZip;

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
	
	public static EasyZip buildEasyZip(File folder, String password) {
		return new EasyZip(folder, password.toCharArray());
	}
	
	public static ByteArrayOutputStream buildByteArrayOutputStream() {
		return new ByteArrayOutputStream();
	}
	
	public static ByteArrayInputStream buildByteArrayInputStream(byte[] content) {
		return new ByteArrayInputStream(content);
	}
	
	public static FileOutputStream buildFileOutputStream(File file) throws FileNotFoundException {
		return new FileOutputStream(file);
	}

}
