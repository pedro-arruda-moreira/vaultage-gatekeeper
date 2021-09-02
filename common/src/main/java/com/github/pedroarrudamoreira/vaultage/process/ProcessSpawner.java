package com.github.pedroarrudamoreira.vaultage.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

import lombok.extern.apachecommons.CommonsLog;
@CommonsLog
public class ProcessSpawner {
	
	private static final AtomicInteger PROCESS_COUNTER = new AtomicInteger(0);
	
	private ProcessSpawner() {
		super();
	}

	public static void executeProcessAndWait(ExecutorService service, String ... command) throws Exception {
		tryExecution(service, command, retVal -> retVal == 0, StringUtils.EMPTY, ".sh", ".cmd", ".bat", ".exe");
	}

	public static void executeProcessAndWait(String ... command) throws Exception {
		tryExecution(null, command, retVal -> retVal == 0, StringUtils.EMPTY, ".sh", ".cmd", ".bat", ".exe");
	}

	public static void executeProcessAndWait(IntFunction<Boolean> failureCodeHandler,
			String ... command) throws Exception {
		tryExecution(null, command, failureCodeHandler, StringUtils.EMPTY, ".sh", ".cmd", ".bat", ".exe");
	}

	private static void tryExecution(ExecutorService service, String[] command,
			IntFunction<Boolean> failureCodeHandler, String ... suffixes) throws Exception {
		String[] realCommand = new String[command.length];
		System.arraycopy(command, 1, realCommand, 1, command.length - 1);
		Exception caughEx = null;
		for(String suffix : suffixes) {
			realCommand[0] = command[0] + suffix;
			try {
				doExecute(service, realCommand, failureCodeHandler);
				return;
			} catch (IOException e) {
				caughEx = e;
			}
		}
		throw caughEx;
	}

	private static void doExecute(ExecutorService service, String[] command,
			IntFunction<Boolean> failureCodeHandler) throws Exception {
		Process process = Runtime.getRuntime().exec(command);
		String commandString = String.join(" ", command);
		final int processNumber = PROCESS_COUNTER.incrementAndGet();
		log.info(String.format("Process number %d is [%s]", processNumber, commandString));
		buildLogThread(process, commandString, Level.INFO, service, processNumber);
		buildLogThread(process, commandString, Level.SEVERE, service, processNumber);
		int retVal = process.waitFor();
		if(!failureCodeHandler.apply(retVal)) {
			throw new RuntimeException(String.format("command %s failed with status %d",
					commandString, retVal));
		}
	}

	private static void buildLogThread(Process process, String commandString, Level level,
			ExecutorService service, int processNumber) throws UnsupportedEncodingException {
		InputStreamReader isr = null;
		InputStream input;
		if(level == Level.INFO) {
			input = process.getInputStream();
		} else {
			input = process.getErrorStream();
		}
		if(SystemUtils.IS_OS_WINDOWS) {
			isr = new InputStreamReader(input, "IBM850");
		} else {
			isr = new InputStreamReader(input);
		}
		BufferedReader br = new BufferedReader(isr);
		Runnable logRunnable = () -> {
			try {
				String line = null;
				while (process.isAlive() && (line = br.readLine()) != null) {
					if(StringUtils.isBlank(line)) {
						continue;
					}
					Object logLine = buildLog(processNumber, line);
					if(level == Level.INFO) {
						log.info(logLine);
					} else {
						log.error(logLine);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		};
		if(service != null) {
			service.execute(logRunnable);
		} else {
			ObjectFactory.buildThread(logRunnable, buildThreadName(processNumber, level)).start();
		}
	}

	private static String buildThreadName(int processNumber, Level level) {
		return String.format("logger-thread-[%d] (%s)", processNumber, level);
	}

	private static Object buildLog(int processNumber, String line) {
		return String.format("[%d]  -> [%s]", processNumber, line);
	}
}
