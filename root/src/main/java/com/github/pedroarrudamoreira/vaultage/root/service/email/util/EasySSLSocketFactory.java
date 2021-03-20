package com.github.pedroarrudamoreira.vaultage.root.service.email.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.lang3.StringUtils;

import internal.com.sun.mail.util.MailSSLSocketFactory;
import lombok.Cleanup;

public class EasySSLSocketFactory extends MailSSLSocketFactory {
	
	public EasySSLSocketFactory(String protocol, String trustStoreLocation, String trustStorePassword, 
			String keyStoreLocation, String keyStorePassword, String privateKeyPassword)
			throws Exception {
		super(protocol);
		if(StringUtils.isNotBlank(trustStoreLocation)) {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(
					TrustManagerFactory.getDefaultAlgorithm());
			final KeyStore ks = loadKeyStore(trustStoreLocation, trustStorePassword);
			tmf.init(ks);
			this.setTrustManagers(tmf.getTrustManagers());
		}
		if(StringUtils.isNotBlank(keyStoreLocation)) {
			KeyManagerFactory tmf = KeyManagerFactory.getInstance(
					KeyManagerFactory.getDefaultAlgorithm());
			final KeyStore ks = loadKeyStore(keyStoreLocation, keyStorePassword);
			tmf.init(ks, privateKeyPassword.toCharArray());
			this.setKeyManagers(tmf.getKeyManagers());
		}
	}

	private KeyStore loadKeyStore(String trustStoreLocation, String trustStorePassword) throws KeyStoreException,
			FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException {
		final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		@Cleanup final FileInputStream stream = new FileInputStream(trustStoreLocation);
		ks.load(stream, trustStorePassword.toCharArray());
		return ks;
	}
	
	
}
