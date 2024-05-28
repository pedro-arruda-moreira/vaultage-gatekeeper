package com.github.pedroarrudamoreira.vaultage.accesscontrol.suppliers;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import com.github.pedroarrudamoreira.vaultage.util.EventLoop;

public class GlobalTokenSupplier implements ITokenSupplier {

	private static final File TOKEN_DIR = new File(new File(SystemUtils.USER_HOME),
			".vaultage_web_app_pwa_tokens");

	public GlobalTokenSupplier(EventLoop eventLoop) {
		if(!TOKEN_DIR.exists() && !TOKEN_DIR.mkdirs()) {
			throw new IllegalStateException("could not create token dir!");
		}


		eventLoop.repeatTask(() -> {
			Instant now = new Date().toInstant();
			File[] tokens = TOKEN_DIR.listFiles();
			for(File token : tokens) {
				if(Math.abs(ChronoUnit.MINUTES.between(
						new Date(token.lastModified()).toInstant(), now)) >= 5) {
					token.delete();
				}
			}
			return true;
		}, 1, TimeUnit.MINUTES);
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
