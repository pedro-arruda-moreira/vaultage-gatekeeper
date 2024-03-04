package com.github.pedroarrudamoreira.vaultage.root.util;

import org.quartz.spi.ThreadPool;

public interface ThreadPoolExt extends ThreadPool {
    void setThreadCount(int count);
}
