package com.cyecize.summer.utils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtils {

    private static final List<Class<?>> primitiveTypes = List.of(
            Byte.class, byte.class,
            Short.class, short.class,
            Integer.class, int.class,
            Long.class, long.class,
            Float.class, float.class,
            Double.class, double.class,
            Boolean.class, boolean.class,
            Character.class, char.class,
            String.class,
            BigInteger.class,
            BigDecimal.class
    );

    /**
     * Gets all fields recursively including all parent fields.
     *
     * @param type the type of the object.
     * @return collection with all fields from parent classes.
     */
    public static List<Field> getAllFieldsRecursively(Class<?> type) {
        final List<Field> fields = new ArrayList<>(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null && type.getSuperclass() != Object.class) {
            fields.addAll(getAllFieldsRecursively(type.getSuperclass()));
        }

        return fields;
    }

    /**
     * Gets field generic type if present or just the field type.
     */
    public static String getFieldGenericType(Field field) {
        return field.getGenericType().getTypeName();
    }

    public static boolean isPrimitive(Class<?> cls) {
        return primitiveTypes.contains(cls);
    }
}
