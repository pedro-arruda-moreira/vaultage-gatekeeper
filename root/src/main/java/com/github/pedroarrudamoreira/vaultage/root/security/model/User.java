package com.github.pedroarrudamoreira.vaultage.root.security.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

import lombok.Getter;
import lombok.Setter;

@JsonSerialize
@Getter
@Setter
public class User {
	@JsonProperty("data-dir")
	private String dataDir;
	
	@JsonProperty
	private Integer port;
	
	@JsonProperty("basic-password")
	private String password;
	
	@JsonProperty("vaultage-username")
	private String vaultageUsername;
	
	@JsonProperty
	private String email;
	
	@JsonProperty("backup-config")
	private Map<String, Object> backupConfig;
	
	public String getDataDir() {
		if(dataDir == null) {
			dataDir = ObjectFactory.normalizePath(SessionController.getApplicationContext().getEnvironment().resolvePlaceholders(
					"${user.home}/.vaultage"));
		}
		return dataDir;
	}
	
	public Integer getPort() {
		if(port == null) {
			port = 3000;
		}
		return port;
	}
	
}
