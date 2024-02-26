package com.github.pedroarrudamoreira.vaultage.root.util;

import org.quartz.SchedulerConfigException;
import org.quartz.spi.ThreadPool;

import com.github.pedroarrudamoreira.vaultage.util.EventLoop;
import org.springframework.web.context.ContextLoader;

public class EventLoopQuartzThreadPool implements ThreadPool {

	private final EventLoop eventLoop;

	public EventLoopQuartzThreadPool() {
		eventLoop = ContextLoader.getCurrentWebApplicationContext().getBean(EventLoop.class);
	}

	private static final int THE_ONLY_THREAD = 1;

	@Override
	public boolean runInThread(Runnable runnable) {
		eventLoop.execute(runnable::run);
		return true;
	}

	@Override
	public int blockForAvailableThreads() {
		return THE_ONLY_THREAD;
	}

	@Override
	public void initialize() throws SchedulerConfigException {
		// no-op
	}

	@Override
	public void shutdown(boolean waitForJobsToComplete) {
		// no-op
	}

	@Override
	public int getPoolSize() {
		return THE_ONLY_THREAD;
	}

	@Override
	public void setInstanceId(String schedInstId) {
		// no-op
	}

	@Override
	public void setInstanceName(String schedName) {
		// no-op
	}
	
	public void setThreadCount(int count) {
		// no-op
	}


}
