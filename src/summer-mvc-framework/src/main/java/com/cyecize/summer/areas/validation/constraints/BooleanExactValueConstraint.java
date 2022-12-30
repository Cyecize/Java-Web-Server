package com.cyecize.summer.areas.validation.constraints;

import com.cyecize.summer.areas.validation.interfaces.ConstraintValidator;
import com.cyecize.summer.common.annotations.Component;

@Component
public class BooleanExactValueConstraint implements ConstraintValidator<BooleanExactValue, Boolean> {
    private boolean expectedVal;

    @Override
    public void initialize(BooleanExactValue constraintAnnotation) {
        this.expectedVal = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Boolean field, Object bindingModel) {
        return field != null && field.equals(this.expectedVal);
    }
}
