package com.cyecize.summer.areas.security.annotations;

import com.cyecize.summer.areas.security.enums.AuthorizationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for specifying the type of security for one or a group of action methods.
 * <p>
 * Annotate a controller with this annotation and it will automatically be added to all action methods inside
 * that controller.
 * Annotating a specific action methods with this annotation will overwrite the annotation on the controller.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface PreAuthorize {
    AuthorizationType value() default AuthorizationType.LOGGED_IN;

    String role() default "";
}
