package com.github.pedroarrudamoreira.vaultage.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class AtomicIntegerSupplier implements Function<Object[], AtomicInteger> {
    @Override
    public AtomicInteger apply(Object[] objects) {
        return new AtomicInteger((Integer) objects[0]);
    }
}
