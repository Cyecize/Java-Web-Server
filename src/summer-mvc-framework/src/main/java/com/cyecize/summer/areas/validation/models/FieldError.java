package com.cyecize.summer.areas.validation.models;

public class FieldError {

    private final String object;

    private final String fieldName;

    private final String message;

    private final Object rejectedValue;

    private final Integer row;

    public FieldError(String objectName, String fieldName, String errorMessage, Object rejectedValue, Integer row) {
        this.object = objectName;
        this.fieldName = fieldName;
        this.message = errorMessage;
        this.rejectedValue = rejectedValue;
        this.row = row;
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

    public Integer getRow() {
        return this.row;
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
