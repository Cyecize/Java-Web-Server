package com.cyecize.summer.areas.validation.constraints;

import com.cyecize.summer.areas.validation.interfaces.ConstraintValidator;
import com.cyecize.summer.common.annotations.Component;

@Component
public class MinConstraint implements ConstraintValidator<Min, Object> {

    private double minValue;

    @Override
    public void initialize(Min constraintAnnotation) {
        this.minValue = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Object field, Object bindingModel) {
        double val = 0D;

        if (field != null) {
            val = Double.valueOf(String.valueOf(field));
        }

        return field != null && minValue <= val;
    }
}
