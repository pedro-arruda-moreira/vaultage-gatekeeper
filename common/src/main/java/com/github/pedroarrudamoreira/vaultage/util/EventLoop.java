package com.github.pedroarrudamoreira.vaultage.util;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class EventLoop {
	@NoArgsConstructor
	@AllArgsConstructor
	private static class RunnableWrapper implements Runnable {
		@Setter
		private Runnable runnable;
		
		private final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		
		@Override
		public void run() {
			Thread.currentThread().setContextClassLoader(loader);
			runnable.run();
		}
		
	}
	
	private EventLoop() {
		super();
	}

	private static final ScheduledThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(1,
			new BasicThreadFactory.Builder().daemon(
					true).namingPattern("vaultage gatekeeper event loop thread").build());


	public static void repeatTask(final Supplier<Boolean> task, final long timeAmmount,
			final TimeUnit timeUnit) {
		final RunnableWrapper wrapper = new RunnableWrapper();
		Runnable command = new Runnable() {

			@Override
			public void run() {
				if(task.get()) {
					schedule(wrapper, timeAmmount, timeUnit);
				}
			}
		};
		wrapper.setRunnable(command);
		execute(wrapper);
	}

	public static ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		return EXECUTOR.schedule(wrapIfNecessary(command), delay, unit);
	}

	public static void execute(Runnable command) {
		EXECUTOR.execute(wrapIfNecessary(command));
	}

	private static Runnable wrapIfNecessary(Runnable command) {
		return command instanceof RunnableWrapper ? command : new RunnableWrapper(command);
	}

	public static void shutdown() {
		EXECUTOR.shutdown();
	}

	

}
