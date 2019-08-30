package com.cyecize.summer.areas.routing.utils;

import java.math.BigDecimal;
import java.math.BigInteger;

public class PrimitiveTypeDataResolver {

    /**
     * Checks if the type matches one of the primitive types and tries to parse the result.
     * If the type is not primitive, return null.
     */
    public Object resolve(Class<?> primitiveType, String data) {
        if (primitiveType == Byte.class || primitiveType == byte.class) {
            return this.tryParse(primitiveType, () -> Byte.parseByte(data));
        }

        if (primitiveType == Short.class || primitiveType == short.class) {
            return this.tryParse(primitiveType, () -> Short.parseShort(data));
        }

        if (primitiveType == Integer.class || primitiveType == int.class) {
            return this.tryParse(primitiveType, () -> Integer.parseInt(data));
        }

        if (primitiveType == Long.class || primitiveType == long.class) {
            return this.tryParse(primitiveType, (() -> Long.parseLong(data)));
        }

        if (primitiveType == Float.class || primitiveType == float.class) {
            return this.tryParse(primitiveType, (() -> Float.parseFloat(data)));
        }

        if (primitiveType == Double.class || primitiveType == double.class) {
            return this.tryParse(primitiveType, (() -> Double.parseDouble(data)));
        }

        if (primitiveType == Boolean.class || primitiveType == boolean.class) {
            return this.tryParse(primitiveType, (() -> Boolean.parseBoolean(data)));
        }

        if (primitiveType == char.class || primitiveType == Character.class) {
            return this.tryParse(primitiveType, () -> data.charAt(0));
        }

        if (primitiveType == BigInteger.class) {
            return this.tryParse(primitiveType, () -> new BigInteger(data));
        }

        if (primitiveType == BigDecimal.class) {
            return this.tryParse(primitiveType, () -> new BigDecimal(data));
        }

        if (primitiveType == String.class) {
            return data;
        }

        return null;
    }

    /**
     * Returns the default value of the type (null if the type is not primitive).
     */
    public Object defaultValue(Class<?> type) {
        if (type == byte.class || type == short.class || type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0f;
        if (type == double.class) return 0.0d;
        if (type == char.class) return '\u0000';
        if (type == boolean.class) return false;

        return null;
    }

    /**
     * Accepts functional interface and tries to call the function.
     * If there is an exception, return the default value of that type.
     */
    private Object tryParse(Class<?> type, TryParse function) {
        try {
            return function.parse();
        } catch (Exception ex) {
            return this.defaultValue(type);
        }
    }
}
