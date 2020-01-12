package com.cyecize.javache.utils;

import java.util.function.Supplier;

public class PrimitiveTypeDataResolver {

    private static final String CANNOT_CONVERT_PARAMETER_FORMAT = "Cannot convert \"%s\" into %s type.";

    private Class<?> currentType;

    private String currentData;

    public Object resolve(Class<?> primitiveType, String data) {
        this.currentType = primitiveType;
        this.currentData = data;

        if (primitiveType == byte.class || primitiveType == Byte.class) {
            return this.tryParse(() -> Byte.parseByte(data));
        }
        if (primitiveType == short.class || primitiveType == Short.class) {
            return this.tryParse(() -> Short.parseShort(data));
        }
        if (primitiveType == int.class || primitiveType == Integer.class) {
            return this.tryParse(() -> Integer.parseInt(data));
        }
        if (primitiveType == long.class || primitiveType == Long.class) {
            return this.tryParse((() -> Long.parseLong(data)));
        }
        if (primitiveType == float.class || primitiveType == Float.class) {
            return this.tryParse((() -> Float.parseFloat(data)));
        }
        if (primitiveType == double.class || primitiveType == Double.class) {
            return this.tryParse((() -> Double.parseDouble(data)));
        }
        if (primitiveType == boolean.class || primitiveType == Boolean.class) {
            return this.tryParse((() -> Boolean.parseBoolean(data)));
        }
        if (primitiveType == char.class || primitiveType == Character.class) {
            return this.tryParse((() -> data.charAt(0)));
        }
        if (primitiveType == String.class) {
            return data;
        }

        return null;
    }

    private Object tryParse(Supplier<Object> function) {
        try {
            return function.get();
        } catch (Exception ex) {
            throw new IllegalArgumentException(String.format(
                    CANNOT_CONVERT_PARAMETER_FORMAT, this.currentData, this.currentType.getName()
            ));
        }
    }
}