package com.cyecize.summer.areas.validation.constraints;

import com.cyecize.summer.areas.validation.exceptions.ValidationException;
import com.cyecize.summer.areas.validation.interfaces.ConstraintValidator;
import com.cyecize.summer.common.annotations.Component;

import java.lang.reflect.Field;
import java.util.Arrays;

@Component
public class FieldMatchConstraint implements ConstraintValidator<FieldMatch, Object> {

    private static final String MATCHING_FIELD_NOT_FOUND_FORMAT = "Field \"%s\" was not found";

    private String fieldName;

    @Override
    public void initialize(FieldMatch constraintAnnotation) {
        this.fieldName = constraintAnnotation.fieldToMatch();
    }

    @Override
    public boolean isValid(Object fieldVal, Object bindingModel) {
        Field matchingField = Arrays.stream(bindingModel.getClass().getDeclaredFields()).filter(f -> f.getName().equals(this.fieldName))
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
            return fieldVal == null;
        }

        return matchingVal.equals(fieldVal);
    }
}
