package com.cyecize.summer.areas.routing.models.annotationModels;

import java.lang.annotation.Annotation;

@FunctionalInterface
public interface AnnotationHandler<T extends Annotation> {
    AnnotationExtractedValue getAnnotationValue(T annotation);
}
