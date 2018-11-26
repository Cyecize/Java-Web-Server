package com.cyecize.summer.areas.validation.services;

import com.cyecize.summer.areas.validation.interfaces.BindingResult;

public interface ObjectValidationService {

    void validateBindingModel(Object bindingModel, BindingResult bindingResult);
}
