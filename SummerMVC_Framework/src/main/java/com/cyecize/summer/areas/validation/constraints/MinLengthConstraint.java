package com.cyecize.summer.areas.validation.constraints;

import com.cyecize.summer.areas.routing.interfaces.MultipartFile;
import com.cyecize.summer.areas.validation.interfaces.ConstraintValidator;
import com.cyecize.summer.common.annotations.Component;

import java.util.Collection;
import java.util.Map;

@Component
public class MinLengthConstraint implements ConstraintValidator<MinLength, Object> {

    private long minLen;

    @Override
    public void initialize(MinLength constraintAnnotation) {
        this.minLen = constraintAnnotation.length();
    }

    @Override
    public boolean isValid(Object field, Object bindingModel) {
        if (field == null) return 0 >= this.minLen;

        if (MultipartFile.class.isAssignableFrom(field.getClass())) {
            return ((MultipartFile) field).getUploadedFile().getFileLength() >= this.minLen;
        }

        if (Collection.class.isAssignableFrom(field.getClass())) {
            return ((Collection) field).size() >= this.minLen;
        }

        if (Map.class.isAssignableFrom(field.getClass())) {
            return ((Map) field).size() >= this.minLen;
        }

        return (field + "").length() >= this.minLen;
    }
}
