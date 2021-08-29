package com.github.pedroarrudamoreira.vaultage.util;

import java.io.File;
import java.io.StringWriter;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.mail.Authenticator;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class ObjectFactory {
	
	public static final String PRESENT = "__present__";
	
	private ObjectFactory() {
		super();
	}
	
	public static Properties buildProperties() {
		return new Properties();
	}
	
	public static Thread buildThread(Runnable run, String name) {
		return new Thread(run, name);
	}
	
	public static AtomicInteger buildAtomicInteger(int value) {
		return new AtomicInteger(value);
	}
	
	public static Session buildEmailSession(Properties properties, Authenticator authenticator) {
		return Session.getDefaultInstance(properties, authenticator);
	}
	
	public static MimeMessage buildMimeMessage(Session session) {
		return new MimeMessage(session);
	}
	
	public static StringWriter buildStringWriter() {
		return new StringWriter();
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

}
