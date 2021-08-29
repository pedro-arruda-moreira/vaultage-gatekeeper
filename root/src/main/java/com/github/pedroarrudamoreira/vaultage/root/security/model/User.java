package com.github.pedroarrudamoreira.vaultage.root.security.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;

import lombok.Getter;
import lombok.Setter;

@JsonSerialize
@Getter
@Setter
public class User {
	@JsonProperty
	private String dataDir;
	
	@JsonProperty
	private Integer port;
	
	@JsonProperty("basic_password")
	private String password;
	
	@JsonProperty("vaultage_username")
	private String vaultageUsername;
	
	@JsonProperty
	private String email;
	
	@JsonProperty
	private Map<String, Object> backupConfig;
	
	public String getDataDir() {
		if(dataDir == null) {
			dataDir = SessionController.getApplicationContext().getEnvironment().resolvePlaceholders(
					"${user.home}/.vaultage");
		}
		return dataDir;
	}
	
}
