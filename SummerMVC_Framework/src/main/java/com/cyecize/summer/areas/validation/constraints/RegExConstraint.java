package com.cyecize.summer.areas.validation.constraints;

import com.cyecize.summer.areas.validation.interfaces.ConstraintValidator;
import com.cyecize.summer.common.annotations.Component;

@Component
public class RegExConstraint implements ConstraintValidator<RegEx, String> {

    private String pattern;

    @Override
    public void initialize(RegEx constraintAnnotation) {
        this.pattern = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String field, Object bindingModel) {
        return field != null && field.matches(this.pattern);
    }
}
