package com.github.pedroarrudamoreira.vaultage.root.util;

import java.util.Properties;

public final class ObjectFactory {
	
	private ObjectFactory() {
		super();
	}
	
	public static Properties buildProperties() {
		return new Properties();
	}

}
