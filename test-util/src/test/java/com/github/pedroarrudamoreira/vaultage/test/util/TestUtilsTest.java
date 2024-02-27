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

    @Mock
    @FactoryArgumentTypes
    private File file2;

    @Mock
    @FactoryArgumentTypes({String.class, String.class})
    private File file3;

    @Mock
    @FactoryArgumentTypes({String.class, Class.class})
    private File file4;


    @Test
    public void mustWork() {
        File obtained = factory.build(File.class, "something");
        Assert.assertEquals(file, obtained);
        Assert.assertNull(factory.build(File.class, this.getClass()));
        File obtained2 = factory.build(File.class);
        Assert.assertEquals(file2, obtained2);
        File obtained3 = factory.build(File.class, "something", "something");
        Assert.assertEquals(file3, obtained3);
        File obtained4 = factory.build(File.class, "something", this.getClass());
        Assert.assertEquals(file4, obtained4);
    }
}
