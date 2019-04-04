package com.cyecize.summer.areas.routing.utils;

import java.math.BigDecimal;
import java.math.BigInteger;

public class PrimitiveTypeDataResolver {

    /**
     * Checks if the type matches one of the primitive types and tries to parse the result.
     * If the type is not primitive, return null.
     */
    public Object resolve(Class<?> primitiveType, String data) {
        if (primitiveType == byte.class) {
            Object parsed = this.tryParse(() -> Byte.parseByte(data));
            if (parsed != null) return parsed;
            return 0;
        }

        if (primitiveType == Byte.class) {
            return this.tryParse(() -> Byte.parseByte(data));
        }

        if (primitiveType == short.class) {
            Object parsed = this.tryParse(() -> Short.parseShort(data));
            if (parsed != null) return parsed;
            return 0;
        }

        if (primitiveType == Short.class) {
            return this.tryParse(() -> Short.parseShort(data));
        }

        if (primitiveType == int.class) {
            Object parsed = this.tryParse(() -> Integer.parseInt(data));
            if (parsed != null) return parsed;
            return 0;
        }

        if (primitiveType == Integer.class) {
            return this.tryParse(() -> Integer.parseInt(data));
        }

        if (primitiveType == long.class) {
            Object parsed = this.tryParse((() -> Long.parseLong(data)));
            if (parsed != null) return parsed;
            return 0L;
        }

        if (primitiveType == Long.class) {
            return this.tryParse((() -> Long.parseLong(data)));
        }

        if (primitiveType == float.class) {
            Object parsed = this.tryParse((() -> Float.parseFloat(data)));
            if (parsed != null) return parsed;
            return 0.0F;
        }

        if (primitiveType == Float.class) {
            return this.tryParse((() -> Float.parseFloat(data)));
        }

        if (primitiveType == double.class) {
            Object parsed = this.tryParse((() -> Double.parseDouble(data)));
            if (parsed != null) return parsed;
            return 0.0D;
        }

        if (primitiveType == Double.class) {
            return this.tryParse((() -> Double.parseDouble(data)));
        }

        if (primitiveType == boolean.class) {
            Object parsed = this.tryParse((() -> Boolean.parseBoolean(data)));
            if (parsed != null) return parsed;
            return false;
        }

        if (primitiveType == Boolean.class) {
            return this.tryParse((() -> Boolean.parseBoolean(data)));
        }

        if (primitiveType == char.class) {
            Object parsed = this.tryParse((() -> data.charAt(0)));
            if (parsed != null) return parsed;
            return '\u0000';
        }

        if (primitiveType == Character.class) {
            this.tryParse((() -> data.charAt(0)));
        }

        if (primitiveType == BigInteger.class) {
            return this.tryParse(() -> new BigInteger(data));
        }

        if (primitiveType == BigDecimal.class) {
            return this.tryParse(() -> new BigDecimal(data));
        }

        if (primitiveType == String.class) {
            return data;
        }

        return null;
    }

    /**
     * Accepts functional interface and tries to call the function.
     * If there is an exception, return null.
     */
    private Object tryParse(TryParse function) {
        try {
            return function.parse();
        } catch (Exception ex) {
            return null;
        }
    }
}
