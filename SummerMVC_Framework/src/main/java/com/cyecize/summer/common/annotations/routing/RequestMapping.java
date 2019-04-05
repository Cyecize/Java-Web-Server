package com.cyecize.summer.common.annotations.routing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequestMapping {
    String value();

    String produces() default "text/html";

    HttpMethod method() default HttpMethod.GET;
}
