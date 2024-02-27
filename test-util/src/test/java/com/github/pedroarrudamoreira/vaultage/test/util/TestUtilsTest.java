package com.github.pedroarrudamoreira.vaultage.test.util;

import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;

@RunWith(MockitoJUnitRunner.class)
public class TestUtilsTest extends AbstractTest {

    @Mock
    private ObjectFactory factory;

    @Mock
    @FactoryArgumentTypes({String.class})
    private File file;


    @Test
    public void mustWork() {
        File obtained = factory.build(File.class, "something");
        Assert.assertEquals(file, obtained);
        Assert.assertNull(factory.build(File.class));
        Assert.assertNull(factory.build(File.class, this.getClass()));
    }
}
