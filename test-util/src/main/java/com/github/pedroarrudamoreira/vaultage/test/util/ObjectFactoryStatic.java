package com.github.pedroarrudamoreira.vaultage.test.util;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ObjectFactoryStatic {

    Class<?> clazz();

    String name();

    String[] args() default {};
}
