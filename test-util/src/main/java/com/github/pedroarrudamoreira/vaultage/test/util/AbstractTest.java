package com.github.pedroarrudamoreira.vaultage.test.util;

import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;
import lombok.SneakyThrows;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Field;
import java.util.function.BiFunction;

public abstract class AbstractTest {

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    @SneakyThrows
    @Before
    public void prepare() {
        prepareMockStatic();
        configureFactory(this);
    }

    public static void configureFactory(AbstractTest test) throws IllegalAccessException {
        ObjectFactory objF = null;
        Field[] allFields = test.getClass().getDeclaredFields();
        for (Field fld : allFields) {
            fld.setAccessible(true);
            if (fld.getAnnotation(Mock.class) == null) {
                continue;
            }
            if (fld.getType() != ObjectFactory.class) {
                continue;
            }
            objF = (ObjectFactory) fld.get(test);
            break;
        }
        if (objF == null) {
            return;
        }
        ObjectFactory.setFactory(objF);
        for (Field fld : allFields) {
            fld.setAccessible(true);
            if (fld.getAnnotation(ObjectFactoryInject.class) == null) {
                continue;
            }
            ObjectFactoryInject f = fld.getAnnotation(ObjectFactoryInject.class);
            Class<?> type = fld.getType();
            configureBuilderInMockito(test, objF, fld, f, type);
        }
        for (Field fld : allFields) {
            fld.setAccessible(true);
            if (fld.getAnnotation(ObjectFactoryStatic.class) == null) {
                continue;
            }
            ObjectFactoryStatic f = fld.getAnnotation(ObjectFactoryStatic.class);
            configureStaticInMockito(test, objF, fld, f);
        }
        for (Field fld : allFields) {
            fld.setAccessible(true);
            if (fld.getAnnotation(ObjectFactorySupplier.class) == null) {
                continue;
            }
            ObjectFactorySupplier f = fld.getAnnotation(ObjectFactorySupplier.class);
            configureSupplierInMockito(test, objF, fld, f);
        }
    }

    private static void configureSupplierInMockito(AbstractTest test, ObjectFactory objF, Field fld, ObjectFactorySupplier f) {

        Answer<Object> answer = i -> fld.get(test);
        Mockito.eq(f.clazz());
        if (f.args() == null || f.args().length == 0) {
            Mockito.when(objF.doFromSupplier(null, null)).thenAnswer(answer);
            return;
        }
        String[] args = f.args();
        final BiFunction<Object, Integer, Boolean> matcher = (arg, idx) -> {
            if (args.length > idx) {
                Object expected = parseExpression(test, args[idx]);
                return arg == expected || arg.equals(expected);
            }
            return true;
        };
        final int size = args.length;
        for (int i = 0; i < size; i++) {
            final int index = i;
            Mockito.argThat((arg) -> matcher.apply(arg, index));
        }
        switch (size) {
            case 1:
                Mockito.when(objF.doFromSupplier(null, (Object) null)).thenAnswer(answer);
                break;
            case 2:
                Mockito.when(objF.doFromSupplier(null, null, null)).thenAnswer(answer);
                break;
            case 3:
                Mockito.when(objF.doFromSupplier(null, null, null, null)).thenAnswer(answer);
                break;
            case 4:
                Mockito.when(objF.doFromSupplier(null, null, null, null, null)).thenAnswer(answer);
                break;
            default:
                throw new RuntimeException("max 4");

        }
    }

    @SneakyThrows
    private static void configureStaticInMockito(AbstractTest test, ObjectFactory objF, Field fld, ObjectFactoryStatic f) {
        Answer<Object> answer = i -> fld.get(test);
        Mockito.eq(f.clazz());
        Mockito.eq(f.name());
        if (f.args() == null || f.args().length == 0) {
            Mockito.when(objF.doInvokeStatic(null, null)).thenAnswer(answer);
            return;
        }
        String[] args = f.args();
        final BiFunction<Object, Integer, Boolean> matcher = (arg, idx) -> {
            if (args.length > idx) {
                Object expected = parseExpression(test, args[idx]);
                return arg == expected || arg.equals(expected);
            }
            return true;
        };
        final int size = args.length;
        for (int i = 0; i < size; i++) {
            final int index = i;
            Mockito.argThat((arg) -> matcher.apply(arg, index));
        }
        switch (size) {
            case 1:
                Mockito.when(objF.doInvokeStatic(null, null, (Object) null)).thenAnswer(answer);
                break;
            case 2:
                Mockito.when(objF.doInvokeStatic(null, null, null, null)).thenAnswer(answer);
                break;
            case 3:
                Mockito.when(objF.doInvokeStatic(null, null, null, null, null)).thenAnswer(answer);
                break;
            case 4:
                Mockito.when(objF.doInvokeStatic(null, null, null, null, null, null)).thenAnswer(answer);
                break;
            default:
                throw new RuntimeException("max 4");

        }

    }

    private static Object parseExpression(Object root, String expression) {
        final String trimExp = expression.trim();
        if (trimExp.startsWith("#{") && trimExp.endsWith("}")) {
            final String exp = trimExp.substring(2, trimExp.length() - 1);
            Expression parsedExpression = PARSER.parseExpression(exp);
            return parsedExpression.getValue(root);
        }
        return trimExp;
    }

    private static <T> void configureBuilderInMockito(AbstractTest test, ObjectFactory objF, Field fld, ObjectFactoryInject f, Class<T> type) throws IllegalAccessException {
        Answer<Object> answer = i -> fld.get(test);
        Class<?>[] argumentTypes = f.types();
        Mockito.eq(type);
        if (argumentTypes == null || argumentTypes.length == 0) {
            Mockito.when(objF.doBuild(null)).thenAnswer(answer);
            return;
        }
        String[] values = f.values();
        final BiFunction<Object, Integer, Boolean> matcher = (arg, idx) -> {
            boolean isCompatibleType = argumentTypes[idx].isAssignableFrom(arg.getClass());
            if (!isCompatibleType) {
                return false;
            }
            if (values.length > idx) {
                Object expected = parseExpression(test, values[idx]);
                return arg == expected || arg.equals(expected);
            }
            return true;
        };
        final int size = argumentTypes.length;
        for (int i = 0; i < size; i++) {
            final int index = i;
            Mockito.argThat((arg) -> matcher.apply(arg, index));
        }
        switch (size) {
            case 1:
                Mockito.when(objF.doBuild(null, (Object) null)).thenAnswer(answer);
                break;
            case 2:
                Mockito.when(objF.doBuild(null, null, null)).thenAnswer(answer);
                break;
            case 3:
                Mockito.when(objF.doBuild(null, null, null, null)).thenAnswer(answer);
                break;
            case 4:
                Mockito.when(objF.doBuild(null, null, null, null, null)).thenAnswer(answer);
                break;
            default:
                throw new RuntimeException("max 4");

        }
    }

    public static void prepareMockStatic() {
        PrepareForTest anno = getAnnotation();
        if (anno == null) {
            return;
        }
        Class<?>[] classes = anno.value();
        Class<?>[] classesCopy = new Class[classes.length - 1];
        System.arraycopy(classes, 1, classesCopy, 0, classes.length - 1);
        PowerMockito.mockStatic(classes[0], classesCopy);
    }

    private static PrepareForTest getAnnotation() {
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
