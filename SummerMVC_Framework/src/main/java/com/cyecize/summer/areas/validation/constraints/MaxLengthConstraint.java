package com.cyecize.summer.areas.validation.constraints;

import com.cyecize.summer.areas.validation.interfaces.ConstraintValidator;
import com.cyecize.summer.common.annotations.Component;

import java.util.List;

@Component
public class MaxLengthConstraint implements ConstraintValidator<MaxLength, Object> {

    private int maxLen;

    @Override
    public void initialize(MaxLength constraintAnnotation) {
        this.maxLen = constraintAnnotation.length();
    }

    @Override
    public boolean isValid(Object field, Object bindingModel) {
        if (field instanceof List) {
            return ((List) field).size() <= this.maxLen;
        }
        if (field == null) return 0 <= this.maxLen;
        return (field + "").length() <= this.maxLen;
    }
}
