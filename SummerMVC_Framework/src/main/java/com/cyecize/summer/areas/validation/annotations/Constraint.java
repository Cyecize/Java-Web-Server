package com.cyecize.summer.areas.validation.annotations;

import com.cyecize.summer.areas.validation.interfaces.ConstraintValidator;

import java.lang.annotation.*;

@Documented
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Constraint {
    Class<? extends ConstraintValidator<?, ?>> validatedBy();
}
