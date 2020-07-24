package com.cyecize.solet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate an implementation of {@link HttpSolet} with this annotation for
 * broccolina to map the solet route.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WebSolet {
    String value() default "";

    boolean loadOnStartUp() default true;
}
