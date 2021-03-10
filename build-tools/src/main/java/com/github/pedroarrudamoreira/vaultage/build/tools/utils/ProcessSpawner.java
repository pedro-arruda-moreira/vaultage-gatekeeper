package com.github.pedroarrudamoreira.vaultage.build.tools.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.function.IntFunction;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

public class ProcessSpawner {
	private ProcessSpawner() {
		super();
	}

	public static void executeProcessAndWait(String ... command) throws Exception {
		tryExecution(command, retVal -> retVal == 0, StringUtils.EMPTY, ".cmd", ".bat", ".exe");
	}

	public static void executeProcessAndWait(IntFunction<Boolean> failureCodeHandler,
			String ... command) throws Exception {
		tryExecution(command, failureCodeHandler, StringUtils.EMPTY, ".cmd", ".bat", ".exe");
	}

	private static void tryExecution(String[] command,
			IntFunction<Boolean> failureCodeHandler, String ... suffixes) throws Exception {
		String[] realCommand = new String[command.length];
		System.arraycopy(command, 1, realCommand, 1, command.length - 1);
		Exception caughEx = null;
		for(String suffix : suffixes) {
			realCommand[0] = command[0] + suffix;
			try {
				doExecute(realCommand, failureCodeHandler);
				return;
			} catch (IOException e) {
				caughEx = e;
			}
		}
		throw caughEx;
	}

	private static void doExecute(String[] command,
			IntFunction<Boolean> failureCodeHandler) throws Exception {
		Process process = Runtime.getRuntime().exec(command);
		buildLogThread(process.getInputStream());
		buildLogThread(process.getErrorStream());
		int retVal = process.waitFor();
		if(!failureCodeHandler.apply(retVal)) {
			throw new RuntimeException(String.format("command %s failed with status %d",
					Arrays.deepToString(command), retVal));
		}
	}

	private static void buildLogThread(InputStream input) throws UnsupportedEncodingException {
		InputStreamReader isr = null;
		if(SystemUtils.IS_OS_WINDOWS) {
			isr = new InputStreamReader(input, "IBM850");
		} else {
			isr = new InputStreamReader(input);
		}
		BufferedReader br = new BufferedReader(isr);
		new Thread(() -> {
			try {
				String line = null;
				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}
}
