package com.github.pedroarrudamoreira.vaultage.process;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import com.github.pedroarrudamoreira.vaultage.util.EventLoop;
import com.github.pedroarrudamoreira.vaultage.util.IOUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
@CommonsLog
@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public class ProcessSpawner {
	
	private static final IntFunction<Boolean> DEFAULT_FAILURE_HANDLER = retVal -> retVal == 0;
	
	private static final AtomicInteger PROCESS_COUNTER = new AtomicInteger(0);
	
	private static final String [] SUFFIXES = new String[] {StringUtils.EMPTY, ".sh", ".cmd", ".bat", ".exe"};
	
	private final String[] command;
	
	private final IntFunction<Boolean> failureCodeHandler;
	
	private final Consumer<String> logConsumer;

	public static Process executeProcess(Consumer<String> logConsumer, String ... command) throws Exception {
		return new ProcessSpawner(command, DEFAULT_FAILURE_HANDLER, logConsumer).tryExecution(false);
	}

	public static void executeProcessAndWait(String ... command) throws Exception {
		new ProcessSpawner(command, DEFAULT_FAILURE_HANDLER, null).tryExecution(true);
	}

	public static void executeProcessAndWait(IntFunction<Boolean> failureCodeHandler,
			String ... command) throws Exception {
		new ProcessSpawner(command, failureCodeHandler, null).tryExecution(true);
	}

	private Process tryExecution(boolean doWait) throws Exception {
		String[] realCommand = new String[command.length];
		System.arraycopy(command, 1, realCommand, 1, command.length - 1);
		Exception caughEx = null;
		for(String suffix : SUFFIXES) {
			realCommand[0] = command[0] + suffix;
			try {
				return doExecute(doWait, realCommand);
			} catch (IOException e) {
				caughEx = e;
			}
		}
		throw caughEx;
	}

	private Process doExecute(boolean doWait, String[] realCommand) throws Exception {
		Process process = Runtime.getRuntime().exec(realCommand);
		String commandString = String.join(" ", realCommand);
		final int processNumber = PROCESS_COUNTER.incrementAndGet();
		log.info(String.format("Process number %d is [%s]", processNumber, commandString));
		buildLogThread(process, commandString, Level.INFO, processNumber);
		buildLogThread(process, commandString, Level.SEVERE, processNumber);
		if(!doWait) {
			return process;
		}
		int retVal = process.waitFor();
		if(!failureCodeHandler.apply(retVal)) {
			throw new RuntimeException(String.format("command %s failed with status %d",
					commandString, retVal));
		}
		return null;
	}

	private void buildLogThread(Process process, String commandString, Level level,
			int processNumber) throws UnsupportedEncodingException {
		InputStream input;
		input = getStream(process, level);
		Supplier<Boolean> logRunnable = () -> {
			try {
				int availableBytes = input.available();
				if(availableBytes < 1) {
					return process.isAlive();
				}
				BufferedReader br = getBytesFromStreamAsReader(input, availableBytes);
				String line = null;
				while((line = br.readLine()) != null) {
					if(StringUtils.isBlank(line)) {
						continue;
					}
					if(logConsumer != null && line.startsWith("%MSG:")) {
						logConsumer.accept(line.substring(5));
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
			return process.isAlive();
		};
		EventLoop.repeatTask(logRunnable, 700l, TimeUnit.MILLISECONDS);
	}

	private BufferedReader getBytesFromStreamAsReader(InputStream input, int availableBytes)
			throws IOException, UnsupportedEncodingException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtils.copy(input, baos, availableBytes);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		InputStreamReader isr = buildReader(bais);
		BufferedReader br = new BufferedReader(isr, 1);
		return br;
	}

	private InputStream getStream(Process process, Level level) {
		InputStream input;
		if(level == Level.INFO) {
			input = process.getInputStream();
		} else {
			input = process.getErrorStream();
		}
		return input;
	}

	private InputStreamReader buildReader(InputStream input) throws UnsupportedEncodingException {
		InputStreamReader isr;
		if(SystemUtils.IS_OS_WINDOWS) {
			isr = new InputStreamReader(input, "IBM850");
		} else {
			isr = new InputStreamReader(input);
		}
		return isr;
	}

	private static Object buildLog(int processNumber, String line) {
		return String.format("[%d]  -> [%s]", processNumber, line);
	}
}
