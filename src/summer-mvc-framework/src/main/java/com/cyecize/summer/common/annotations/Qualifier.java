package com.cyecize.summer.common.annotations;

import com.cyecize.ioc.annotations.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@AliasFor(com.cyecize.ioc.annotations.Qualifier.class)
public @interface Qualifier {
    String value();
}
