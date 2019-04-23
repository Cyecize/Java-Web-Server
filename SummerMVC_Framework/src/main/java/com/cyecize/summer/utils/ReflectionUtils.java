package com.cyecize.summer.utils;

import com.cyecize.summer.common.annotations.Autowired;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtils {

    /**
     * Gets all fields recursively including all parent fields.
     *
     * @param type the type of the object.
     * @return collection with all fields from parent classes.
     */
    public static List<Field> getAllFieldsRecursively(Class<?> type) {
        List<Field> fields = new ArrayList<>(Arrays.asList(type.getDeclaredFields()));

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

    /**
     * Gets all public constructs and filters the one that has @Autowired annotation or gets the first if no
     * annotation is present.
     *
     * @param type - the target class.
     * @return @Autowired annotated constructor or the fist if no annotations is present.
     */
    public static <T> Constructor<T> findConstructor(Class<T> type) {
        Constructor<T>[] constructors = (Constructor<T>[]) type.getConstructors();

        return Arrays.stream(constructors)
                .filter(c -> c.isAnnotationPresent(Autowired.class))
                .findFirst().orElse(constructors[0]);
    }
}
