package com.github.pedroarrudamoreira.vaultage.root.util.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class KeyAndIV {
	@Getter
	private final byte[] key;
	@Getter
	private final byte[] iv;
	
	public static KeyAndIV fromString(String key) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-384");
		byte[] digested = md.digest(key.getBytes(StandardCharsets.ISO_8859_1));
		byte[] aesKey = new byte[32];
		byte[] iv = new byte[16];
		System.arraycopy(digested, 0, aesKey, 0, 32);
		System.arraycopy(digested, 32, iv, 0, 16);
		return new KeyAndIV(aesKey, iv);
	}

}
