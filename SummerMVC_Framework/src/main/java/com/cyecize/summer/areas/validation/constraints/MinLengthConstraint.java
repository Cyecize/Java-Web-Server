package com.cyecize.summer.areas.validation.constraints;

import com.cyecize.summer.areas.validation.interfaces.ConstraintValidator;
import com.cyecize.summer.common.annotations.Component;

import java.util.List;

@Component
public class MinLengthConstraint implements ConstraintValidator<MinLength, Object> {

    private int minLen;

    @Override
    public void initialize(MinLength constraintAnnotation) {
        this.minLen = constraintAnnotation.length();
    }

    @Override
    public boolean isValid(Object field, Object bindingModel) {
        if (field instanceof List) {
            return ((List)field).size() >= this.minLen;
        }
        if (field == null) return 0 >= this.minLen;
        return (field + "").length() >= this.minLen;
    }
}
