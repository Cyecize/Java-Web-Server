package com.cyecize.summer.areas.validation.objectmapper;

import com.cyecize.summer.areas.startup.services.DependencyContainer;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter;
import com.fasterxml.jackson.databind.util.Converter;

/**
 * Allows for creating Jackson ({@link JsonSerializer}, {@link JsonDeserializer},
 * {@link KeyDeserializer}, {@link TypeResolverBuilder}, {@link TypeIdResolver})
 * beans with autowiring against {@link DependencyContainer}.
 *
 * <p>As of Spring 4.3, this overrides all factory methods in {@link HandlerInstantiator},
 * including non-abstract ones and recently introduced ones from Jackson 2.4 and 2.5:
 * for {@link ValueInstantiator}, {@link ObjectIdGenerator}, {@link ObjectIdResolver},
 * {@link PropertyNamingStrategy}, {@link Converter}, {@link VirtualBeanPropertyWriter}.
 */
public class ObjectMapperHandlerInstantiator extends HandlerInstantiator {

    private final DependencyContainer dependencyContainer;

    public ObjectMapperHandlerInstantiator(DependencyContainer dependencyContainer) {
        this.dependencyContainer = dependencyContainer;
    }

    @Override
    public JsonDeserializer<?> deserializerInstance(DeserializationConfig config,
                                                    Annotated annotated, Class<?> implClass) {

        return (JsonDeserializer<?>) this.dependencyContainer.getService(implClass);
    }

    @Override
    public KeyDeserializer keyDeserializerInstance(DeserializationConfig config,
                                                   Annotated annotated, Class<?> implClass) {

        return (KeyDeserializer) this.dependencyContainer.getService(implClass);
    }

    @Override
    public JsonSerializer<?> serializerInstance(SerializationConfig config,
                                                Annotated annotated, Class<?> implClass) {

        return (JsonSerializer<?>) this.dependencyContainer.getService(implClass);
    }

    @Override
    public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config,
                                                              Annotated annotated, Class<?> implClass) {

        return (TypeResolverBuilder<?>) this.dependencyContainer.getService(implClass);
    }

    @Override
    public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> implClass) {
        return (TypeIdResolver) this.dependencyContainer.getService(implClass);
    }

    /**
     * @since 4.3
     */
    @Override
    public ValueInstantiator valueInstantiatorInstance(MapperConfig<?> config,
                                                       Annotated annotated, Class<?> implClass) {

        return (ValueInstantiator) this.dependencyContainer.getService(implClass);
    }

    /**
     * @since 4.3
     */
    @Override
    public ObjectIdGenerator<?> objectIdGeneratorInstance(MapperConfig<?> config,
                                                          Annotated annotated, Class<?> implClass) {

        return (ObjectIdGenerator<?>) this.dependencyContainer.getService(implClass);
    }

    /**
     * @since 4.3
     */
    @Override
    public ObjectIdResolver resolverIdGeneratorInstance(MapperConfig<?> config,
                                                        Annotated annotated, Class<?> implClass) {

        return (ObjectIdResolver) this.dependencyContainer.getService(implClass);
    }

    /**
     * @since 4.3
     */
    @Override
    public PropertyNamingStrategy namingStrategyInstance(MapperConfig<?> config,
                                                         Annotated annotated, Class<?> implClass) {

        return (PropertyNamingStrategy) this.dependencyContainer.getService(implClass);
    }

    /**
     * @since 4.3
     */
    @Override
    public Converter<?, ?> converterInstance(MapperConfig<?> config,
                                             Annotated annotated, Class<?> implClass) {

        return (Converter<?, ?>) this.dependencyContainer.getService(implClass);
    }

    /**
     * @since 4.3
     */
    @Override
    public VirtualBeanPropertyWriter virtualPropertyWriterInstance(MapperConfig<?> config, Class<?> implClass) {
        return (VirtualBeanPropertyWriter) this.dependencyContainer.getService(implClass);
    }
}
