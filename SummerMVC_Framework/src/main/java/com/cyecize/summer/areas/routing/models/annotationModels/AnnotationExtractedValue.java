package com.cyecize.summer.areas.routing.models.annotationModels;

public class AnnotationExtractedValue {

    private final String httpMethod;

    private final String contentType;

    private final String pattern;

    public AnnotationExtractedValue(String httpMethod, String contentType, String pattern) {
        this.httpMethod = httpMethod;
        this.contentType = contentType;
        this.pattern = pattern;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getContentType() {
        return contentType;
    }

    public String getPattern() {
        return pattern;
    }
}
