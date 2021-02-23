package com.github.pedroarrudamoreira.vaultage.util;

public class ThreadControl {

	private ThreadControl() {
		super();
	}
	
	public static void sleep(long millis) throws InterruptedException {
		Thread.sleep(millis);
	}
	
}
