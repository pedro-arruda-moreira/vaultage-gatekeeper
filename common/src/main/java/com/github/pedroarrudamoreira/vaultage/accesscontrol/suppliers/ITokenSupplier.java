package com.github.pedroarrudamoreira.vaultage.accesscontrol.suppliers;

import java.io.IOException;

public interface ITokenSupplier {
	

	String generateNewToken() throws IOException;
	
	boolean isTokenValid(String token);
	
	boolean removeToken(String uuid);

}
