package com.github.pedroarrudamoreira.vaultage.util;

import lombok.Setter;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;

import java.util.concurrent.*;
import java.util.function.Supplier;

public class EventLoop implements BeanClassLoaderAware {

    @Setter
    private ClassLoader beanClassLoader;

    public interface Task {
        void run();
    }

    static  {
        executor = Executors.newSingleThreadScheduledExecutor(new BasicThreadFactory.Builder().daemon(
                true).namingPattern("vaultage gatekeeper event loop thread").build());
    }

    private static final ScheduledExecutorService executor;


    public void repeatTask(final Supplier<Boolean> task, final long timeAmount,
                           final TimeUnit timeUnit) {
        Task command = new Task() {

            @Override
            public void run() {
                if (task.get()) {
                    schedule(this, timeAmount, timeUnit);
                }
            }
        };
        execute(command);
    }

    public ScheduledFuture<?> schedule(Task command, long delay, TimeUnit unit) {
        return executor.schedule(wrap(command), delay, unit);
    }

    public void execute(Task command) {
        executor.execute(wrap(command));
    }

    private Runnable wrap(Task command) {
        return () -> {
            if(beanClassLoader != null) {
                Thread.currentThread().setContextClassLoader(beanClassLoader);
            }
            command.run();
        };
    }

    public void shutdown() {
        executor.shutdown();
    }


}
