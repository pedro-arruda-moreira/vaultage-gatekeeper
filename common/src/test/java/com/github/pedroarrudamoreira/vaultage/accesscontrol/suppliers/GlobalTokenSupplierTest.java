package com.github.pedroarrudamoreira.vaultage.accesscontrol.suppliers;

import com.github.pedroarrudamoreira.vaultage.test.util.AbstractTest;
import com.github.pedroarrudamoreira.vaultage.test.util.ObjectFactoryStatic;
import com.github.pedroarrudamoreira.vaultage.util.EventLoop;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(JUnit4.class)
public class GlobalTokenSupplierTest extends AbstractTest {

    @Mock
    private EventLoop eventLoop;

    @Mock
    private ObjectFactory objectFactory;

    @ObjectFactoryStatic(
            clazz = Collections.class,
            name = "synchronizedSet",
            args = "{{any}}"
    )
    private static Set<String> tokens = new HashSet<>();

    private static String newToken;

    private static EventLoop.Task deleteTask;

    private GlobalTokenSupplier impl;

    @Before
    public void setup() {
        impl = new GlobalTokenSupplier(eventLoop);
        Mockito.when(eventLoop.schedule(Mockito.any(), Mockito.eq(5l), Mockito.eq(TimeUnit.MINUTES))).thenAnswer(
                i -> {
                    deleteTask = i.getArgument(0, EventLoop.Task.class);
                    return null;
                }
        );
    }

    @Test
    public void test000_createToken() {
        newToken = impl.generateNewToken();
        Assert.assertNotNull(newToken);
        Assert.assertTrue(tokens.contains(newToken));
    }


    @Test
    public void test001_checkToken() {
        Assert.assertTrue(impl.isTokenValid(newToken));
        Assert.assertTrue(tokens.contains(newToken));
    }

    @Test
    public void test002_deleteToken() {
        Assert.assertTrue(impl.removeToken(newToken));
        Assert.assertFalse(tokens.contains(newToken));
        Assert.assertFalse(impl.isTokenValid(newToken));
    }

    @Test
    public void test003_regenerateToken() {
        test000_createToken();
    }

    @Test
    public void test004_expiredToken() {
        deleteTask.run();
        Assert.assertFalse(tokens.contains(newToken));
        Assert.assertFalse(impl.isTokenValid(newToken));
    }



}
