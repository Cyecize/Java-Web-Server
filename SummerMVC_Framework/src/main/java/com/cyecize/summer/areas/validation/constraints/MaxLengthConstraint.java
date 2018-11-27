package com.cyecize.summer.areas.validation.constraints;

import com.cyecize.summer.areas.validation.interfaces.ConstraintValidator;
import com.cyecize.summer.common.annotations.Component;

@Component
public class MaxLengthConstraint implements ConstraintValidator<MaxLength, String> {

    private int maxLen;

    @Override
    public void initialize(MaxLength constraintAnnotation) {
        this.maxLen = constraintAnnotation.length();
    }

    @Override
    public boolean isValid(String field, Object bindingModel) {
        return (field + "").length() <= this.maxLen;
    }
}
