package com.github.pedroarrudamoreira.vaultage.test.util;

import org.junit.Before;

public abstract class AbstractTest implements TestUtils {

    @Before
    public void prepare() {
        doPrepareForTest();
    }

}
