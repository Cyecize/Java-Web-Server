package com.cyecize.summer.areas.routing.models.annotationModels;

import java.lang.annotation.Annotation;

public class ActionAnnotationHandlerContainer<T extends Annotation> {

    private final AnnotationHandler<T> annotationHandler;

    private final Class<T> annotationType;

    public ActionAnnotationHandlerContainer(Class<T> annotationType, AnnotationHandler<T> annotationHandler) {
        this.annotationType = annotationType;
        this.annotationHandler = annotationHandler;
    }

    public Class<T> getAnnotationType() {
        return this.annotationType;
    }

    public AnnotationExtractedValue getAnnotationValue(T annotation) {
        return this.annotationHandler.getAnnotationValue(annotation);
    }
}