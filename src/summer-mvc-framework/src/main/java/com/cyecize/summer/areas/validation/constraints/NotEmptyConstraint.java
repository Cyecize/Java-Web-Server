package com.cyecize.summer.areas.validation.constraints;

import com.cyecize.summer.areas.validation.interfaces.ConstraintValidator;
import com.cyecize.summer.common.annotations.Component;

@Component
public class NotEmptyConstraint implements ConstraintValidator<NotEmpty, String> {

    @Override
    public boolean isValid(String field, Object bindingModel) {
        return field != null && !field.trim().equals("");
    }
}
