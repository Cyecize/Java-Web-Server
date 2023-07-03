package com.cyecize.summer.areas.validation.services;

import com.cyecize.ioc.utils.AliasFinder;
import com.cyecize.summer.areas.startup.services.DependencyContainer;
import com.cyecize.summer.areas.validation.annotations.Constraint;
import com.cyecize.summer.areas.validation.annotations.RejectedValueExclude;
import com.cyecize.summer.areas.validation.annotations.Valid;
import com.cyecize.summer.areas.validation.exceptions.ErrorDuringValidationException;
import com.cyecize.summer.areas.validation.interfaces.BindingResult;
import com.cyecize.summer.areas.validation.interfaces.ConstraintValidator;
import com.cyecize.summer.areas.validation.models.FieldError;
import com.cyecize.summer.common.annotations.Component;
import com.cyecize.summer.utils.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

public class ObjectValidationServiceImpl implements ObjectValidationService {

    private static final String NO_MESSAGE_PRESENT_FORMAT = "No message method present in annotation \"%s\".";

    private static final String ADD_ANNOTATION_TO_CLASS_FORMAT = "Validator \"%s\" not annotated with \"%s\"";

    private final DependencyContainer dependencyContainer;

    public ObjectValidationServiceImpl(DependencyContainer dependencyContainer) {
        this.dependencyContainer = dependencyContainer;
    }

    /**
     * Iterate all fields of the given object and for each field perform validation.
     */
    @Override
    public void validateBindingModel(Object bindingModel, BindingResult bindingResult) {
        this.validateBindingModel(bindingModel, bindingResult, null);
    }

    /**
     * @param bindingModel  -
     * @param bindingResult -
     * @param row           - If the validation is for a collection, this works as an indicator to know which element in the
     *                      collection has an error.
     */
    private void validateBindingModel(Object bindingModel, BindingResult bindingResult, Integer row) {
        if (bindingModel == null || ReflectionUtils.isPrimitive(bindingModel.getClass())) {
            return;
        }

        //If the type is collection, validate each of its elements.
        if (Collection.class.isAssignableFrom(bindingModel.getClass())) {
            int rowCount = 0;
            for (Object element : (Collection<?>) bindingModel) {
                this.validateBindingModel(element, bindingResult, rowCount++);
            }
            return;
        }

        try {
            for (Field declaredField : ReflectionUtils.getAllFieldsRecursively(bindingModel.getClass())) {
                this.validateField(declaredField, bindingModel, bindingResult, row);
                if (declaredField.isAnnotationPresent(Valid.class)) {
                    this.validateBindingModel(declaredField.get(bindingModel), bindingResult);
                }
            }
        } catch (Throwable th) {
            throw new ErrorDuringValidationException(th.getMessage(), th);
        }
    }

    /**
     * Iterates all annotations for the given field.
     * If an annotation is annotated with {@link Constraint}, get the constraint validator.
     * If the constraintValidator is null, throw {@link ErrorDuringValidationException}.
     * Reload the validator if lifeSpan == REQUEST
     * <p>
     * If the validator returns false, add new {@link FieldError} to the {@link BindingResult}.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void validateField(Field field, Object bindingModel, BindingResult bindingResult, Integer row) {
        try {
            field.setAccessible(true);
            Object fieldVal = field.get(bindingModel);

            for (Annotation currentAnnotation : field.getDeclaredAnnotations()) {
                if (!currentAnnotation.annotationType().isAnnotationPresent(Constraint.class)) {
                    continue;
                }

                final Constraint constraint = currentAnnotation.annotationType().getAnnotation(Constraint.class);
                final ConstraintValidator validator = this.dependencyContainer.getService(constraint.validatedBy());

                if (validator == null) {
                    throw new ErrorDuringValidationException(String.format(
                            ADD_ANNOTATION_TO_CLASS_FORMAT,
                            constraint.validatedBy().getName(),
                            Component.class.getName()
                    ));
                }

                validator.initialize(currentAnnotation);

                if (validator.isValid(fieldVal, bindingModel)) {
                    continue;
                }

                bindingResult.addNewError(this.createFieldError(bindingModel, field, currentAnnotation, fieldVal, row));
            }

        } catch (IllegalAccessException e) {
            throw new ErrorDuringValidationException(e.getMessage(), e);
        }
    }

    private FieldError createFieldError(Object bindingModel,
                                        Field field,
                                        Annotation currentAnnotation,
                                        Object fieldVal,
                                        Integer row) {
        // Hide field value from the response if it is configured to do so.
        if (fieldVal != null) {
            if (AliasFinder.isAnnotationPresent(field.getDeclaredAnnotations(), RejectedValueExclude.class)) {
                fieldVal = null;
            } else if (AliasFinder.isAnnotationPresent(
                    fieldVal.getClass().getDeclaredAnnotations(), RejectedValueExclude.class)) {
                fieldVal = null;
            }
        }

        return new FieldError(
                bindingModel.getClass().getName(),
                field.getName(),
                this.getAnnotationMessage(currentAnnotation),
                fieldVal,
                row
        );
    }

    /**
     * Gets the message method of a given annotation using reflection.
     *
     * @throws ErrorDuringValidationException if the annotation is missing the message method.
     */
    private String getAnnotationMessage(Annotation annotation) {
        try {
            final Method message = annotation.annotationType().getDeclaredMethod("message");
            message.setAccessible(true);
            return (String) message.invoke(annotation);
        } catch (NoSuchMethodException e) {
            throw new ErrorDuringValidationException(String.format(NO_MESSAGE_PRESENT_FORMAT, annotation.getClass().getName()));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ErrorDuringValidationException(e.getMessage(), e);
        }
    }
}
