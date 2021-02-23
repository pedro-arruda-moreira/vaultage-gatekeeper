package com.github.pedroarrudamoreira.vaultage.accesscontrol;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.lang3.SystemUtils;

public final class TokenManager {

	private static final File TOKEN_DIR = new File(new File(SystemUtils.USER_HOME),
			".vaultage_web_app_pwa_tokens");

	static {
		if(!TOKEN_DIR.exists() && !TOKEN_DIR.mkdirs()) {
			throw new IllegalStateException("could not create token dir!");
		}
	}
	
	private TokenManager() {
		super();
	}

	public static String generateNewToken() throws IOException {
		String uuid = UUID.randomUUID().toString();
		new File(TOKEN_DIR, uuid).createNewFile();
		return uuid;
	}
	
	public static boolean isTokenValid(String uuid) {
		return new File(TOKEN_DIR, uuid).exists();
	}
	
	public static boolean removeToken(String uuid) {
		return new File(TOKEN_DIR, uuid).delete();
	}

}
