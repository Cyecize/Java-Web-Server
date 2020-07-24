package com.cyecize.summer.areas.validation.constraints;

import com.cyecize.summer.areas.validation.annotations.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = MinConstraint.class)
public @interface Min {
    String message() default "Number is below minimum threshold.";

    double value();
}
