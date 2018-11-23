package com.cyecize.summer.areas.validation.constraints;

import com.cyecize.summer.areas.validation.interfaces.ConstraintValidator;
import com.cyecize.summer.common.annotations.Component;

@Component
public class NotNullConstraint implements ConstraintValidator<NotNull, Object> {
    @Override
    public boolean isValid(Object field, Object bindingModel) {
        return field != null;
    }
}
