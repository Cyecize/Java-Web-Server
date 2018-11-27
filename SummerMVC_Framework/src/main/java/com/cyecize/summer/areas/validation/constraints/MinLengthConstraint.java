package com.cyecize.summer.areas.validation.constraints;

import com.cyecize.summer.areas.validation.interfaces.ConstraintValidator;
import com.cyecize.summer.common.annotations.Component;

@Component
public class MinLengthConstraint implements ConstraintValidator<MinLength, String> {

    private int minLen;

    @Override
    public void initialize(MinLength constraintAnnotation) {
        this.minLen = constraintAnnotation.length();
    }

    @Override
    public boolean isValid(String field, Object bindingModel) {
        return (field + "").length() >= this.minLen;
    }
}
