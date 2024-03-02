package com.github.pedroarrudamoreira.vaultage.test.util;

import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;
import lombok.SneakyThrows;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Field;
import java.util.function.BiFunction;

public interface TestUtils {

    SpelExpressionParser PARSER = new SpelExpressionParser();


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
            if (fld.getAnnotation(ObjectFactoryInject.class) == null) {
                continue;
            }
            ObjectFactoryInject f = fld.getAnnotation(ObjectFactoryInject.class);
            Class<?> type = fld.getType();
            configureMockito(objF, fld, f, type);
        }
    }

    default Object parseExpression(String expression) {
        final String trimExp = expression.trim();
        if(trimExp.startsWith("#{") && trimExp.endsWith("}")) {
            final String exp = trimExp.substring(2, trimExp.length() - 1);
            Expression parsedExpression = PARSER.parseExpression(exp);
            return parsedExpression.getValue(this);
        }
        return trimExp;
    }

    default <T> void configureMockito(ObjectFactory objF, Field fld, ObjectFactoryInject f, Class<T> type) throws IllegalAccessException {
        Class<?>[] argumentTypes = f.types();
        String[] values = f.values();
        T o = (T) fld.get(this);
        if (argumentTypes == null || argumentTypes.length == 0) {
            Mockito.when(objF.build(Mockito.eq(type))).thenReturn(o);
        } else {
            Mockito.eq(type);
            final int size = argumentTypes.length;
            final BiFunction<Object, Integer, Boolean> matcher
                    = (arg, idx) -> {
                boolean isCompatibleType = argumentTypes[idx].isAssignableFrom(arg.getClass());
                if(!isCompatibleType) {
                    return false;
                }
                if(values.length > idx) {
                    Object expected = parseExpression(values[idx]);
                    return arg == expected || arg.equals(expected);
                }
                return true;
            };
            for (int i = 0; i < size; i++) {
                final int index = i;
                Mockito.argThat((arg) -> matcher.apply(arg, index));
            }
            switch (size) {
                case 1:
                    Mockito.when(objF.build(null, (Object) null)).thenReturn(o);
                    break;
                case 2:
                    Mockito.when(objF.build(null, null, null)).thenReturn(o);
                    break;
                case 3:
                    Mockito.when(objF.build(null, null, null, null)).thenReturn(o);
                    break;
                case 4:
                    Mockito.when(objF.build(null, null, null, null, null)).thenReturn(o);
                    break;
                default:
                    throw new RuntimeException("max 4");

            }
        }
    }

    static void prepareMockStatic() {
        PrepareForTest anno = getAnnotation();
        if (anno == null) {
            return;
        }
        Class<?>[] classes = anno.value();
        Class<?>[] classesCopy = new Class[classes.length - 1];
        System.arraycopy(classes, 1, classesCopy, 0, classes.length - 1);
        PowerMockito.mockStatic(classes[0], classesCopy);
    }

    static PrepareForTest getAnnotation() {
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
