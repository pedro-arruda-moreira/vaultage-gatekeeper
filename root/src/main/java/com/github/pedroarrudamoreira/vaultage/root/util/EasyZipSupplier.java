package com.github.pedroarrudamoreira.vaultage.root.util;

import com.github.pedroarrudamoreira.vaultage.root.util.zip.EasyZip;

import java.io.File;
import java.util.function.Function;

public class EasyZipSupplier implements Function<Object[], EasyZip> {
    @Override
    public EasyZip apply(Object[] objects) {
        char[] charArray = null;
        String password = (String) objects[1];
        if (password != null) {
            charArray = password.toCharArray();
        }
        return new EasyZip((File) objects[0], charArray, (Boolean) objects[2]);
    }
}
