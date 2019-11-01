package com.cyecize.summer.common.annotations;

import com.cyecize.summer.common.extensions.SessionScopeFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface SessionFactory {

    Class<? extends SessionScopeFactory> value();
}
