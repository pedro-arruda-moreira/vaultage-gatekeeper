package com.github.pedroarrudamoreira.vaultage.test.util;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

public class TestUtils {
	
	private TestUtils() {
		super();
	}
	
	public static void doPrepareForTest() {
		PrepareForTest anno = getAnnotation();
		if(anno == null) {
			return;
		}
		Class<?>[] classes = anno.value();
		Class<?>[] classesCopy = new Class[classes.length - 1];
		System.arraycopy(classes, 1, classesCopy, 0, classes.length - 1);
		PowerMockito.mockStatic(classes[0], classesCopy);
	}

	private static PrepareForTest getAnnotation() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for(StackTraceElement elem : stackTrace) {
			Class<?> clazz;
			try {
				clazz = Class.forName(elem.getClassName());
			} catch (ClassNotFoundException e) {
				continue;
			}
			PrepareForTest annotation = clazz.getAnnotation(PrepareForTest.class);
			if(annotation != null) {
				return annotation;
			}
		}
		return null;
	}

}
