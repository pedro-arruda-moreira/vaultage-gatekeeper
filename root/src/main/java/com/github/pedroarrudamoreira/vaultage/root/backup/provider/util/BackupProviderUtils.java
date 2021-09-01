package com.github.pedroarrudamoreira.vaultage.root.backup.provider.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.github.pedroarrudamoreira.vaultage.root.security.model.User;

public final class BackupProviderUtils {
	
	private BackupProviderUtils() {
		super();
	}
	
	@SuppressWarnings("unchecked")
	public static String getParamsAsString(Object params) {
		if(params instanceof List) {
			StringBuilder bld = new StringBuilder();
			for(Object current : (List<Object>) params) {
				bld.append(current.toString()).append(',');
			}
			bld.setLength(bld.length() - 1);
			return bld.toString();
		}
		return params.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static List<String> getParamsAsStringList(Object params) {
		if(params instanceof List) {
			List<String> result = new ArrayList<>();
			for(Object current : (List<Object>) params) {
				result.add(current.toString());
			}
			return result;
		}
		return Collections.singletonList(params.toString());
	}
	public static String createFileName(User vaultageUser) {
		return String.format("vaultage_backup_%s_%s.zip",
				vaultageUser.getUserId(),
				new SimpleDateFormat("yyyyMMdd").format(new Date()));
	}

}
