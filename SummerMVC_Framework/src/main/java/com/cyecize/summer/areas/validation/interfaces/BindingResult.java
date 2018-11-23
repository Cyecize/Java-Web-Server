package com.cyecize.summer.areas.validation.interfaces;

import com.cyecize.summer.areas.validation.models.FieldError;

import java.util.List;

public interface BindingResult {

    void addNewError(FieldError fieldError);

    boolean hasErrors();

    List<FieldError> getFieldErrors(String field);

    List<FieldError> getErrors();
}
