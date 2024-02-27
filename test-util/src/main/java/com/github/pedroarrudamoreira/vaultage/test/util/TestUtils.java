package com.github.pedroarrudamoreira.vaultage.test.util;

import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;
import lombok.SneakyThrows;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.lang.reflect.Field;

public interface TestUtils {


    @SneakyThrows
    default void doPrepareForTest() {
        prepareMockStatic();
        configureFactory();
    }

    default void configureFactory() throws IllegalAccessException {
        ObjectFactory objF = null;
        Field[] allFields = this.getClass().getDeclaredFields();
        for (Field fld : allFields) {
            fld.setAccessible(true);
            if (fld.getAnnotation(Mock.class) == null) {
                continue;
            }
            if (fld.getType() != ObjectFactory.class) {
                continue;
            }
            objF = (ObjectFactory) fld.get(this);
            break;
        }
        if (objF == null) {
            return;
        }
        for (Field fld : allFields) {
            fld.setAccessible(true);
            if (fld.getAnnotation(Mock.class) == null) {
                continue;
            }
            if (fld.getAnnotation(FactoryArgumentTypes.class) == null) {
                continue;
            }
            FactoryArgumentTypes f = fld.getAnnotation(FactoryArgumentTypes.class);
            Class<?> type = fld.getType();
            configureMockito(objF, fld, f, type);
        }
    }

    default <T> void configureMockito(ObjectFactory objF, Field fld, FactoryArgumentTypes f, Class<T> type) throws IllegalAccessException {
        Class<?>[] argumentTypes = f.value();
        T o = (T) fld.get(this);
        if (argumentTypes == null || argumentTypes.length == 0) {
            Mockito.when(objF.build(Mockito.eq(type), Mockito.any())).thenReturn(o);
        } else {
            Mockito.eq(type);
            Mockito.argThat((arg) -> {
                if (arg instanceof Object[]) {
                    Object[] argVector = (Object[]) arg;
                    for (int i = 0; i < argVector.length; i++) {
                        if (!argumentTypes[i].isAssignableFrom(argVector[i].getClass())) {
                            return false;
                        }
                    }
                }
                return argumentTypes[0].isAssignableFrom(arg.getClass());
            });
            Mockito.when(objF.build(null, (Object) null)).thenReturn(o);
        }
    }

    default void prepareMockStatic() {
        PrepareForTest anno = getAnnotation();
        if (anno == null) {
            return;
        }
        Class<?>[] classes = anno.value();
        Class<?>[] classesCopy = new Class[classes.length - 1];
        System.arraycopy(classes, 1, classesCopy, 0, classes.length - 1);
        PowerMockito.mockStatic(classes[0], classesCopy);
    }

    default PrepareForTest getAnnotation() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement elem : stackTrace) {
            Class<?> clazz;
            try {
                clazz = Class.forName(elem.getClassName());
            } catch (ClassNotFoundException e) {
                continue;
            }
            PrepareForTest annotation = clazz.getAnnotation(PrepareForTest.class);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

}
