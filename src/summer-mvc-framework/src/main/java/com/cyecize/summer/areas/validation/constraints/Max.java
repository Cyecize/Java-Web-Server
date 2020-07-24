package com.cyecize.summer.areas.validation.constraints;

import com.cyecize.summer.areas.validation.annotations.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = MaxConstraint.class)
public @interface Max {
    String message() default "Number is over maximum threshold.";

    double value();
}
