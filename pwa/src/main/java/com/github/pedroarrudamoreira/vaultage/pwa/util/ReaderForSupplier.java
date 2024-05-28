package com.github.pedroarrudamoreira.vaultage.pwa.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.util.function.Function;

public class ReaderForSupplier implements Function<Object[], ObjectReader> {


    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public ObjectReader apply(Object[] objects) {
        return mapper.readerFor((Class) objects[0]);
    }
}
