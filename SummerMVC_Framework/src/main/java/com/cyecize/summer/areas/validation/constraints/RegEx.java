package com.cyecize.summer.areas.validation.constraints;

import com.cyecize.summer.areas.validation.annotations.Constraint;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = RegExConstraint.class)
public @interface RegEx {
    String value();

    String message() default "The value did not match the pattern.";
}
