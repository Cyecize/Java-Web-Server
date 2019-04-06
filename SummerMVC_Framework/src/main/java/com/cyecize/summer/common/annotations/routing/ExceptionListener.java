package com.cyecize.summer.common.annotations.routing;

import com.cyecize.summer.constants.ContentTypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionListener {
    Class<?> value();

    String produces() default ContentTypes.NONE;
}
