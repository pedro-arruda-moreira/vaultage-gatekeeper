package com.github.pedroarrudamoreira.vaultage.test.util;

import com.github.pedroarrudamoreira.vaultage.util.AtomicIntegerSupplier;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(MockitoJUnitRunner.class)
public class TestUtilsTest extends AbstractTest {

    public final String blaValue = "  bla ";

    @Mock
    private ObjectFactory factory;

    @Mock
    @ObjectFactoryInject(types = {String.class})
    private File file;

    @Mock
    @ObjectFactoryInject
    private File file2;

    @Mock
    @ObjectFactoryInject(
            types = {String.class, String.class},
            values = {"something", "something"}
    )
    private File file3;

    @Mock
    @ObjectFactoryInject(types = {String.class, Class.class})
    private File file4;

    @Mock
    @ObjectFactoryInject(
            types = {String.class, String.class},
            values = {"#{blaValue.trim()}", "something"}
    )
    private File file5;

    @ObjectFactoryStatic(clazz = UUID.class, name = "randomUUID")
    private final String fakeUUID = "not-a-uuid :)";

    @ObjectFactoryStatic(
            clazz = UUID.class,
            name = "randomUUID",
            args = {"#{blaValue.trim()}"}
    )
    private final String fakeUUID2 = "not-a-uuid2 :)";

    @ObjectFactorySupplier(
            clazz = AtomicIntegerSupplier.class,
            args = {"#{40}"}
    )
    @Mock
    private AtomicInteger atomic;

    @Test
    public void mustWork() {
        File obtained = ObjectFactory.build(File.class, "something");
        Assert.assertEquals(file, obtained);
        Assert.assertNull(factory.doBuild(File.class, this.getClass()));
        File obtained2 = factory.doBuild(File.class);
        Assert.assertEquals(file2, obtained2);
        File obtained3 = factory.doBuild(File.class, "something", "something");
        Assert.assertEquals(file3, obtained3);
        File obtained4 = factory.doBuild(File.class, "something", this.getClass());
        Assert.assertEquals(file4, obtained4);
        File obtained5 = factory.doBuild(File.class, blaValue.trim(), "something");
        Assert.assertEquals(file5, obtained5);
        String obtainedUUID = factory.doInvokeStatic(UUID.class, "randomUUID");
        Assert.assertEquals(fakeUUID, obtainedUUID);
        String obtainedUUID2 = ObjectFactory.invokeStatic(UUID.class, "randomUUID", blaValue.trim());
        Assert.assertEquals(fakeUUID2, obtainedUUID2);
        AtomicInteger obtainedAtomicInteger = ObjectFactory.buildAtomicInteger(40);
        Assert.assertEquals(atomic, obtainedAtomicInteger);
    }
}
