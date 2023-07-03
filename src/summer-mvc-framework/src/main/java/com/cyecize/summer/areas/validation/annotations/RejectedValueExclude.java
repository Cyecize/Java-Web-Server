package com.cyecize.summer.areas.validation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to annotate a field from your DTO or a class which you would not want to see appear in the
 * HTTP response as a "rejected value" when there is a constraint violation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface RejectedValueExclude {
}
