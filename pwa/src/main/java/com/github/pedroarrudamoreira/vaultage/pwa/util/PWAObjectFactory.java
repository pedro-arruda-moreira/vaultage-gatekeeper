package com.github.pedroarrudamoreira.vaultage.pwa.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class PWAObjectFactory {
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	private PWAObjectFactory() {
		super();
	}
	
	public static ObjectReader readerFor(Class<?> clazz) {
		return MAPPER.readerFor(clazz);
	}

}
