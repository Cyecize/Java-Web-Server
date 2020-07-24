package com.cyecize.summer.areas.validation.interfaces;

import java.lang.annotation.Annotation;

public interface ConstraintValidator<A extends Annotation, T> {

    default void initialize(A constraintAnnotation) {
    }

    boolean isValid(T field, Object bindingModel);
}
