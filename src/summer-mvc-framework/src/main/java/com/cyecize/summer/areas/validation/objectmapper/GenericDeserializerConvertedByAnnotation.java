package com.cyecize.summer.areas.validation.objectmapper;

import com.cyecize.summer.areas.validation.annotations.ConvertedBy;
import com.cyecize.summer.areas.validation.interfaces.DataAdapter;
import com.cyecize.summer.areas.validation.services.DataAdapterStorageService;
import com.cyecize.summer.common.annotations.Component;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.util.StdConverter;

import java.io.IOException;

/**
 * This deserializer is used by {@link ConvertedBy} as a proxy between {@link DataAdapter} and {@link JsonDeserializer}.
 * In case if application/json content type, every property annotated with it will go through this deserializer
 * where the value for the actual {@link DataAdapter} can be obtained and used instead.
 */
@Component
public class GenericDeserializerConvertedByAnnotation extends JsonDeserializer implements ContextualDeserializer {

    private final DataAdapterStorageService dataAdapterStorageService;

    public GenericDeserializerConvertedByAnnotation(DataAdapterStorageService dataAdapterStorageService) {
        this.dataAdapterStorageService = dataAdapterStorageService;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        throw new RuntimeException(String.format(
                "Method deserialize of class %s should never be called!",
                this.getClass().getName()
        ));
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
                                                BeanProperty property) throws JsonMappingException {
        final ConvertedBy annotation = property.getAnnotation(ConvertedBy.class);
        final DataAdapter<?> dataAdapter = this.dataAdapterStorageService.getDataAdapter(annotation.value());

        return new StdDelegatingDeserializer<>(new StdConverter<>() {
            @Override
            public Object convert(Object value) {
                return dataAdapter.resolve(value == null ? null : value.toString(), null);
            }
        });
    }
}
