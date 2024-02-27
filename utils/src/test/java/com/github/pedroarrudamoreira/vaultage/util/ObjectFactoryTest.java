package com.github.pedroarrudamoreira.vaultage.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class ObjectFactoryTest {
    @Test
    public void mustInstantiateObject() {
        File f = new ObjectFactory().build(File.class, "/home");
        Assert.assertNotNull(f);
    }
}
