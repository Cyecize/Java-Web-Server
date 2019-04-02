package com.cyecize.summer.areas.validation.constraints;

import com.cyecize.summer.areas.validation.exceptions.ValidationException;
import com.cyecize.summer.areas.validation.interfaces.ConstraintValidator;
import com.cyecize.summer.common.annotations.Component;
import com.cyecize.summer.utils.ReflectionUtils;

import java.lang.reflect.Field;

@Component
public class FieldMatchConstraint implements ConstraintValidator<FieldMatch, Object> {

    private static final String MATCHING_FIELD_NOT_FOUND_FORMAT = "Field \"%s\" was not found";

    private String fieldName;

    private boolean inverted;

    @Override
    public void initialize(FieldMatch constraintAnnotation) {
        this.fieldName = constraintAnnotation.fieldToMatch();
        this.inverted = constraintAnnotation.inverted();
    }

    @Override
    public boolean isValid(Object fieldVal, Object bindingModel) {
        Field matchingField = ReflectionUtils.getAllFieldsRecursively(bindingModel.getClass()).stream()
                .filter(f -> f.getName().equals(this.fieldName))
                .findFirst().orElse(null);

        if (matchingField == null) {
            throw new ValidationException(String.format(MATCHING_FIELD_NOT_FOUND_FORMAT, this.fieldName));
        }

        matchingField.setAccessible(true);

        Object matchingVal;
        try {
            matchingVal = matchingField.get(bindingModel);
        } catch (IllegalAccessException e) {
            throw new ValidationException(e.getMessage(), e);
        }

        if (matchingVal == null) {
            if (this.inverted) {
                return fieldVal != null;
            }
            return fieldVal == null;
        }

        if (this.inverted) {
            return !matchingVal.equals(fieldVal);
        }
        return matchingVal.equals(fieldVal);
    }
}
