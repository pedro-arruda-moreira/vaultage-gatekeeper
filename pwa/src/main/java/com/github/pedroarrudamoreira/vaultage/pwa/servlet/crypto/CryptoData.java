package com.github.pedroarrudamoreira.vaultage.pwa.servlet.crypto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonSerialize
public class CryptoData {
	private String pin;
	private String genKey;
	@JsonIgnore
	private int attemptsLeft;
}
