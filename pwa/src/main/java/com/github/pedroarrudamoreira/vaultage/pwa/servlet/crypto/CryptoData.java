package com.github.pedroarrudamoreira.vaultage.pwa.servlet.crypto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonSerialize
public class CryptoData implements Serializable {
	private String pin;
	private String genKey;
	@JsonIgnore
	private int attemptsLeft;
}
