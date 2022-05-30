package com.github.pedroarrudamoreira.vaultage.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import lombok.SneakyThrows;

public class EventLoopTest {
	
	@Test(expected = ExecutionException.class)
	@SneakyThrows
	public void test_ExceptionOnOneTask_DoesNotInterfereWithAnother() {
		final boolean[] executed = new boolean[] {false};
		EventLoop.schedule(() -> {
			executed[0] = true;
		}, 1000, TimeUnit.MILLISECONDS);
		final ScheduledFuture<?> scheduled = EventLoop.schedule(() -> {
			throw new NullPointerException();
		}, 500, TimeUnit.MILLISECONDS);
		Thread.sleep(1500);
		Assert.assertTrue("Exception interfered with another task.", executed[0]);
		scheduled.get();
	}

}
