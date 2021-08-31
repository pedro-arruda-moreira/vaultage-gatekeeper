package com.github.pedroarrudamoreira.vaultage.root.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.mail.Authenticator;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import com.github.pedroarrudamoreira.vaultage.root.util.zip.EasyZip;

public class RootObjectFactory {
	private RootObjectFactory() {
		super();
	}
	
	public static ExecutorService buildDaemonExecutorService(int min, int max, int timeoutMinutes, String namingPattern) {
		return new ThreadPoolExecutor(min, max, timeoutMinutes, TimeUnit.MINUTES, new LinkedBlockingQueue<>(),
				new BasicThreadFactory.Builder().daemon(true).namingPattern(namingPattern).build());
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

}
