package com.cyecize.summer.areas.routing.utils;

public class PrimitiveTypeDataResolver {

    public Object resolve(Class<?> primitiveType, String data) {
        if (primitiveType == byte.class || primitiveType == Byte.class) {
            Object parsed = this.tryParse(() -> Byte.parseByte(data));
            if (parsed != null) return parsed;
            return Byte.MIN_VALUE;
        }
        if (primitiveType == short.class || primitiveType == Short.class) {
            Object parsed = this.tryParse(() -> Short.parseShort(data));
            if (parsed != null) return parsed;
            return Short.MIN_VALUE;
        }
        if (primitiveType == int.class || primitiveType == Integer.class) {
            Object parsed = this.tryParse(() -> Integer.parseInt(data));
            if (parsed != null) return parsed;
            return Integer.MIN_VALUE;
        }
        if (primitiveType == long.class || primitiveType == Long.class) {
            Object parsed = this.tryParse((() -> Long.parseLong(data)));
            if (parsed != null) return parsed;
            return Long.MIN_VALUE;
        }
        if (primitiveType == float.class || primitiveType == Float.class) {
            Object parsed = this.tryParse((() -> Float.parseFloat(data)));
            if (parsed != null) return parsed;
            return Float.MIN_VALUE;
        }
        if (primitiveType == double.class || primitiveType == Double.class) {
            Object parsed = this.tryParse((() -> Double.parseDouble(data)));
            if (parsed != null) return parsed;
            return Double.MIN_VALUE;
        }
        if (primitiveType == boolean.class || primitiveType == Boolean.class) {
            Object parsed = this.tryParse((() -> Boolean.parseBoolean(data)));
            if (parsed != null) return parsed;
            return false;
        }
        if (primitiveType == char.class || primitiveType == Character.class) {
            Object parsed = this.tryParse((() -> data.charAt(0)));
            if (parsed != null) return parsed;
            return (char)0;
        }
        if (primitiveType == String.class) {
            return data;
        }
        return null;
    }

    private Object tryParse(TryParse function) {
        try {
            return function.parse();
        } catch (Exception ex) {
            return null;
        }
    }
}
