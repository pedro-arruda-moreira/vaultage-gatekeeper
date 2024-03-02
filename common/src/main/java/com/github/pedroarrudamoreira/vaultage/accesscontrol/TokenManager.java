package com.github.pedroarrudamoreira.vaultage.accesscontrol;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.suppliers.GlobalTokenSupplier;
import com.github.pedroarrudamoreira.vaultage.accesscontrol.suppliers.ITokenSupplier;
import com.github.pedroarrudamoreira.vaultage.accesscontrol.suppliers.SessionTokenSupplier;
import com.github.pedroarrudamoreira.vaultage.util.EventLoop;
import org.springframework.beans.factory.annotation.Autowired;

public class TokenManager {
	
	private final Map<TokenType, ITokenSupplier> suppliers;
	

	public TokenManager(
			@Autowired EventLoop eventLoop,
			@Autowired SessionController sessionController
	) {
		suppliers = new EnumMap<>(TokenType.class);
		suppliers.put(TokenType.GLOBAL, new GlobalTokenSupplier(eventLoop));
		suppliers.put(TokenType.SESSION, new SessionTokenSupplier(sessionController));
	}

	public String generateNewToken(TokenType type) throws IOException {
		return suppliers.get(type).generateNewToken();
	}
	
	public boolean isTokenValid(String uuid, TokenType type) {
		return suppliers.get(type).isTokenValid(uuid);
	}
	
	public boolean removeToken(String uuid) {
		for(ITokenSupplier current : suppliers.values()) {
			if(current.removeToken(uuid)) {
				return true;
			}
		}
		return false;
	}

}
