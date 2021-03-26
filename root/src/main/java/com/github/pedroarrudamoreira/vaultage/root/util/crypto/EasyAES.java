package com.github.pedroarrudamoreira.vaultage.root.util.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
public class EasyAES {

	private static final String AES_SPEC = "AES/CBC/PKCS5Padding";
	private final KeyAndIV keyAndIV;

	private static final String ALGORITHM="AES";

	public byte[] encrypt(byte[] plainText) throws Exception {
		return processAES(plainText, Cipher.ENCRYPT_MODE);
	}

	public byte[] decrypt(byte[] plainText) throws Exception {
		return processAES(plainText, Cipher.DECRYPT_MODE);
	}

	private byte[] processAES(byte[] plainText, int mode)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		SecretKeySpec secretKey = new SecretKeySpec(keyAndIV.getKey(), ALGORITHM);
		IvParameterSpec ivParameterSpec = new IvParameterSpec(keyAndIV.getIv());
		Cipher cipher = Cipher.getInstance(AES_SPEC);
		cipher.init(mode, secretKey, ivParameterSpec);
		return cipher.doFinal(plainText);
	}

}
