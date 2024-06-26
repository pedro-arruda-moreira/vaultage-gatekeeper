package com.github.pedroarrudamoreira.vaultage.util;

import lombok.Setter;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ObjectFactory {
	
	public static final String PRESENT = "__present__";

	@Setter
	private static ObjectFactory factory;

	private static ObjectFactory getFactory() {
		synchronized (ObjectFactory.class) {
			if(factory == null) {
				factory = new ObjectFactory();
			}
		}
		return factory;
	}

	public ObjectFactory() {
		super();
	}
	
	public static AtomicInteger buildAtomicInteger(int value) {
		return getFactory().doFromSupplier(AtomicIntegerSupplier.class, value);
	}
	
	public static UUID generateUUID() {
		return getFactory().doInvokeStatic(UUID.class, "randomUUID");
	}
	
	public static File buildFile(String path) {
		return getFactory().doBuild(File.class, path);
	}
	
	public static File buildFile(File parent, String name) {
		return getFactory().doBuild(File.class, parent, name);
	}
	
	public static String normalizePath(String path) {
		return buildFile(path).getAbsolutePath();
	}
	
	public static OutputStream buildFileOutputStream(File file) {
		return getFactory().doBuild(FileOutputStream.class, file);
	}
	
	public static InputStream buildFileInputStream(File file) {
		return getFactory().doBuild(FileInputStream.class, file);
	}


	public static  <T> T build(Class<T> clazz, Object ... args) {
		return getFactory().doBuild(clazz, args);
	}
	public static <T> T invokeStatic(Class<?> clazz, String name, Object ... args) {
		return getFactory().doInvokeStatic(clazz, name, args);
	}
	
	private final ConcurrentHashMap<Class<?>, Function<Object[], Object>> functionCache = new ConcurrentHashMap<>();

	@SneakyThrows
	public <T> T doBuild(Class<T> clazz, Object ... args) {
		if(args == null || args.length == 0) {
			return clazz.getConstructor().newInstance();
		}
		return clazz.getConstructor(types(args)).newInstance(args);
	}

	public static <T> T fromSupplier(Class<? extends Function<Object[], T>> clazz, Object ... args) {
		return getFactory().doFromSupplier(clazz, args);
	}

	public <T> T doFromSupplier(Class<? extends Function<Object[], T>> clazz, Object ... args) {
		return (T) functionCache.computeIfAbsent(clazz, c -> (Function) doBuild(c)).apply(args);
	}

	@SneakyThrows
	@SuppressWarnings("unchecked")
	public <T> T doInvokeStatic(Class<?> clazz, String name, Object ... args) {
		if(args == null || args.length == 0) {
			return (T) clazz.getMethod(name).invoke(null);
		}
		Class<?>[] theTypes = types(args);
		Method targetMethod = null;
		try {
			targetMethod = clazz.getMethod(name, theTypes);
		} catch (Exception e) {
			targetMethod = null;
		}
		if(Arrays.stream(theTypes).anyMatch(Objects::isNull) || targetMethod == null) {
			Method[] allMethods = clazz.getMethods();
			targetMethod = Arrays.stream(allMethods).filter((m) -> {
				if(!m.getName().equals(name)) {
					return false;
				}
				Class<?>[] parameterTypes = m.getParameterTypes();
				if(parameterTypes.length != theTypes.length) {
					return false;
				}
				int[] index = new int[] {0};
				return Arrays.stream(theTypes).allMatch((currentCheckType) -> {
					if(currentCheckType == null) {
						return true;
					}
					int i = index[0]++;
					return parameterTypes[i].isAssignableFrom(currentCheckType);
				});
			}).collect(Collectors.toList()).get(0);
		}
		return (T) targetMethod.invoke(null, args);
	}

	private Class<?>[] types(Object[] args) {
		Class<?>[] classes = new Class[args.length];
		int i = 0;
		for(Object o : args) {
			if(o == null) {
				continue;
			}
			classes[i++] = o.getClass();
		}
		return classes;
	}

}
