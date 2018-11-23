package com.cyecize.summer.areas.validation.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RedirectedBindingResult {
    private List<FieldError> errors;

    public RedirectedBindingResult() {
        this.errors = new ArrayList<>();
    }

    public RedirectedBindingResult(List<FieldError> errors) {
        this();
        if (errors != null) {
            this.errors = errors;
        }
    }

    public void addNewError(FieldError fieldError) {
        this.errors.add(fieldError);
    }

    public boolean hasErrors() {
        return this.errors.size() > 0;
    }

    public List<FieldError> getFieldErrors(String field) {
        return this.errors.stream().filter(fe -> fe.getFieldName().equals(field)).collect(Collectors.toList());
    }

    public List<FieldError> getErrors() {
        return this.errors;
    }
}
