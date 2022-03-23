package com.cyecize.summer.areas.validation.annotations;

import com.cyecize.summer.areas.validation.objectmapper.GenericDeserializerConvertedByAnnotation;
import com.cyecize.summer.areas.validation.interfaces.DataAdapter;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate an action method parameter of a field in a binding model with this annotation to apply custom logic for
 * deserializing a given object.
 * <p>
 * eg:
 *
 * @ConvertedBy(ProductIdConverter.class) private Product product;
 * <p>
 * Where the request will be sending product ID as a parameter.
 * <p>
 * This annotation works with both application/json and text/plain contents.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
//Annotated with JsonDeserialize so that it can be registered in the com.fasterxml.jackson.databind.ObjectMapper instance.
@JsonDeserialize(using = GenericDeserializerConvertedByAnnotation.class)
@JacksonAnnotationsInside
public @interface ConvertedBy {

    Class<? extends DataAdapter<?>> value();
}
