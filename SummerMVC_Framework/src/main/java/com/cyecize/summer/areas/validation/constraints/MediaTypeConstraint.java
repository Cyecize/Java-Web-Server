package com.cyecize.summer.areas.validation.constraints;

import com.cyecize.summer.areas.routing.interfaces.MultipartFile;
import com.cyecize.summer.areas.validation.interfaces.ConstraintValidator;
import com.cyecize.summer.common.annotations.Component;

@Component
public class MediaTypeConstraint implements ConstraintValidator<MediaType, MultipartFile> {

    private String[] mediaTypes;

    @Override
    public void initialize(MediaType constraintAnnotation) {
        this.mediaTypes = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(MultipartFile field, Object bindingModel) {
        if (field == null) return false;

        String mediaType = field.getUploadedFile().getContentType();
        for (String type : this.mediaTypes) {
            if (type.equals(mediaType)) {
                return true;
            }
        }

        return false;
    }
}
