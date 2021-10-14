package com.github.pedroarrudamoreira.vaultage.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {
	
	private IOUtils() {
		super();
	}
	
	public static int copy(InputStream i, OutputStream o) throws IOException {
		return copy(i, o, Integer.MAX_VALUE);
	}
	
	public static int copy(InputStream i, OutputStream o, int limit) throws IOException {
		if(i instanceof ByteArrayInputStream && i.available() == 0) {
			i.reset();
		}
		int result = 0;
		int len = -1;
		byte[] buff = new byte[Math.min(limit, 1024)];
		while(result < limit && (len = i.read(buff, 0, Math.min(buff.length, limit - result))) > 0) {
			o.write(buff, 0, len);
			result += len;
		}	
		return result;
	}

}
