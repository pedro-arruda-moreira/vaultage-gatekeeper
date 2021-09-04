package com.github.pedroarrudamoreira.vaultage.accesscontrol.suppliers;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

public class GlobalTokenSupplier implements ITokenSupplier {

	private static final File TOKEN_DIR = new File(new File(SystemUtils.USER_HOME),
			".vaultage_web_app_pwa_tokens");

	static {
		if(!TOKEN_DIR.exists() && !TOKEN_DIR.mkdirs()) {
			throw new IllegalStateException("could not create token dir!");
		}
	}

	@Override
	public String generateNewToken() throws IOException {
		String uuid = UUID.randomUUID().toString();
		new File(TOKEN_DIR, uuid).createNewFile();
		return uuid;
	}

	@Override
	public boolean isTokenValid(String token) {
		if(StringUtils.isBlank(token)) {
			return false;
		}
		return new File(TOKEN_DIR, token).exists();
	}

	@Override
	public boolean removeToken(String uuid) {
		return new File(TOKEN_DIR, uuid).delete();
	}

}
