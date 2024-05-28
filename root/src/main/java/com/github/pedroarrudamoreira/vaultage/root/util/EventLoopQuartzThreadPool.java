package com.github.pedroarrudamoreira.vaultage.root.util;

import com.github.pedroarrudamoreira.vaultage.util.EventLoop;
import lombok.Getter;
import org.springframework.web.context.ContextLoader;

public class EventLoopQuartzThreadPool implements ThreadPoolExt {

	@Getter(lazy = true)
	private final EventLoop eventLoop = ContextLoader.getCurrentWebApplicationContext().getBean(EventLoop.class);;

	public EventLoopQuartzThreadPool() {
		super();
	}

	private static final int THE_ONLY_THREAD = 1;

	@Override
	public boolean runInThread(Runnable runnable) {
		getEventLoop().execute(runnable::run);
		return true;
	}

	@Override
	public int blockForAvailableThreads() {
		return THE_ONLY_THREAD;
	}

	@Override
	public void initialize() {
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

	@Override
	public void setThreadCount(int count) {
		// no-op
	}


}
