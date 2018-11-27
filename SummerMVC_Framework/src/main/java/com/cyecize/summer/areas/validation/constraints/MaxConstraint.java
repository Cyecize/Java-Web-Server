package com.cyecize.summer.areas.validation.constraints;

import com.cyecize.summer.areas.validation.interfaces.ConstraintValidator;
import com.cyecize.summer.common.annotations.Component;

@Component
public class MaxConstraint implements ConstraintValidator<Max, Object> {

    private double max;

    @Override
    public void initialize(Max constraintAnnotation) {
        this.max = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Object field, Object bindingModel) {
        double val = Double.valueOf(String.valueOf(field));
        return field != null && max >= val;
    }
}
