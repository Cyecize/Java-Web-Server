package com.cyecize.summer.areas.validation.models;

public class FieldError {

    private String object;

    private String fieldName;

    private Object rejectedValue;

    public FieldError(String objectName, String fieldName, Object rejectedValue) {
        this.object = objectName;
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }

    public String getObject() {
        return this.object;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public Object getRejectedValue() {
        return this.rejectedValue;
    }

    @Override
    public String toString() {
        return "Field error in object '" + this.object + "' on field '" + this.fieldName + "': rejected value [" + this.rejectedValue + "]; ";
    }
}
