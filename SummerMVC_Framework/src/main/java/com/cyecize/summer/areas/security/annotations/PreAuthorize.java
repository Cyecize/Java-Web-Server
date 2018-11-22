package com.cyecize.summer.areas.security.annotations;

import com.cyecize.summer.areas.security.enums.AuthorizationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface PreAuthorize {
    AuthorizationType value() default AuthorizationType.LOGGED_IN;

    String role() default "";
}
