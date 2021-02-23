package com.github.pedroarrudamoreira.vaultage.util;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class ObjectFactory {
	
	private ObjectFactory() {
		super();
	}
	
	public static Properties buildProperties() {
		return new Properties();
	}
	
	public static Thread buildThread(Runnable run, String name) {
		return new Thread(run, name);
	}
	
	public static AtomicInteger buildAtomicInteger(int value) {
		return new AtomicInteger(value);
	}

}
