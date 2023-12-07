package com.github.pedroarrudamoreira.vaultage.util;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

public class EventLoop {

    public EventLoop(String name) {
        executor = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().daemon(
                        true).namingPattern("vaultage gatekeeper event loop thread - " + name).build());
    }

    private final ScheduledThreadPoolExecutor executor;


    public void repeatTask(final Supplier<Boolean> task, final long timeAmount,
                           final TimeUnit timeUnit) {
        Runnable command = new Runnable() {

            @Override
            public void run() {
                if (task.get()) {
                    schedule(this, timeAmount, timeUnit);
                }
            }
        };
        execute(command);
    }

    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return executor.schedule(command, delay, unit);
    }

    public void execute(Runnable command) {
        executor.execute(command);
    }

    public void shutdown() {
        executor.shutdown();
    }


}
