package com.github.pedroarrudamoreira.vaultage.accesscontrol;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.suppliers.GlobalTokenSupplier;
import com.github.pedroarrudamoreira.vaultage.accesscontrol.suppliers.ITokenSupplier;
import com.github.pedroarrudamoreira.vaultage.accesscontrol.suppliers.SessionTokenSupplier;

public final class TokenManager {
	
	private static final Map<TokenType, ITokenSupplier> SUPPLIERS;
	
	static {
		SUPPLIERS = new EnumMap<>(TokenType.class);
		SUPPLIERS.put(TokenType.GLOBAL, new GlobalTokenSupplier());
		SUPPLIERS.put(TokenType.SESSION, new SessionTokenSupplier());
	}
	private TokenManager() {
		super();
	}

	public static String generateNewToken(TokenType type) throws IOException {
		return SUPPLIERS.get(type).generateNewToken();
	}
	
	public static boolean isTokenValid(String uuid, TokenType type) {
		return SUPPLIERS.get(type).isTokenValid(uuid);
	}
	
	public static boolean removeToken(String uuid) {
		for(ITokenSupplier current : SUPPLIERS.values()) {
			if(current.removeToken(uuid)) {
				return true;
			}
		}
		return false;
	}

}
