package com.cyecize.summer.areas.validation.models;

public class FieldError {

    private String object;

    private String fieldName;

    private String message;

    private Object rejectedValue;

    public FieldError(String objectName, String fieldName, String errorMessage, Object rejectedValue) {
        this.object = objectName;
        this.fieldName = fieldName;
        this.message = errorMessage;
        this.rejectedValue = rejectedValue;
    }

    public String getObject() {
        return this.object;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public String getMessage() {
        return this.message;
    }

    public Object getRejectedValue() {
        return this.rejectedValue;
    }

    @Override
    public String toString() {
        return String.format(
                "Field error in object '%s' on field '%s': rejected value [%s]; ",
                this.object,
                this.fieldName,
                this.rejectedValue
        );
    }
}
