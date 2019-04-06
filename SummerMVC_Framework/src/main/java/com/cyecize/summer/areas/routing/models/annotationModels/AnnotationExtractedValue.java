package com.cyecize.summer.areas.routing.models.annotationModels;

import java.util.List;

public class AnnotationExtractedValue {

    private final List<String> httpMethods;

    private final String contentType;

    private final String pattern;

    public AnnotationExtractedValue(List<String> httpMethods, String contentType, String pattern) {
        this.httpMethods = httpMethods;
        this.contentType = contentType;
        this.pattern = pattern;
    }

    public List<String> getHttpMethods() {
        return this.httpMethods;
    }

    public String getContentType() {
        return this.contentType;
    }

    public String getPattern() {
        return this.pattern;
    }
}
