package com.cyecize.summer.areas.validation.services;

import com.cyecize.http.MultipartFile;
import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.SoletConfig;
import com.cyecize.solet.SoletConstants;
import com.cyecize.summer.areas.routing.interfaces.UploadedFile;
import com.cyecize.summer.areas.routing.models.UploadedFileImpl;
import com.cyecize.summer.areas.routing.utils.PrimitiveTypeDataResolver;
import com.cyecize.summer.areas.startup.services.DependencyContainer;
import com.cyecize.summer.areas.validation.annotations.ConvertedBy;
import com.cyecize.summer.areas.validation.exceptions.ObjectBindingException;
import com.cyecize.summer.areas.validation.interfaces.DataAdapter;
import com.cyecize.summer.constants.ContentTypes;
import com.cyecize.summer.utils.ReflectionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import static com.cyecize.summer.constants.GeneralConstants.HTTP_REQUEST_RAW_BODY_PAYLOAD_PARAM;

public class ObjectBindingServiceImpl implements ObjectBindingService {

    private final DependencyContainer dependencyContainer;

    private final DataAdapterStorageService dataAdapters;

    private final PrimitiveTypeDataResolver dataResolver;

    private final ObjectMapper objectMapper;

    private String assetsDir;

    public ObjectBindingServiceImpl(DependencyContainer dependencyContainer, DataAdapterStorageService dataAdapters) {
        this.dependencyContainer = dependencyContainer;
        this.dataAdapters = dataAdapters;
        this.dataResolver = new PrimitiveTypeDataResolver();
        this.objectMapper = dependencyContainer.getService(ObjectMapper.class);
    }

    /**
     * Iterates through fields and for every field checks if there is a data adapter for it.
     * If there is, use the data adapter, otherwise check if the field is MultipartFile or List.
     * Finally consider that the field is primitive.
     * <p>
     * In case content type is application/json, use {@link ObjectMapper} to map body to the provided binding model.
     */
    @Override
    public void populateBindingModel(Object bindingModel) {
        final HttpSoletRequest request = this.dependencyContainer.getService(HttpSoletRequest.class);

        if (request.getBodyParameters() == null) {
            return;
        }

        if (ContentTypes.APPLICATION_JSON.equals(request.getContentType())) {
            this.populateBindingModelFromJsonPayload(
                    request.getBodyParam(HTTP_REQUEST_RAW_BODY_PAYLOAD_PARAM),
                    bindingModel
            );
            return;
        }

        for (Field field : ReflectionUtils.getAllFieldsRecursively(bindingModel.getClass())) {
            field.setAccessible(true);

            Object parsedVal;
            //add other types here
            final String fieldGenericType = ReflectionUtils.getFieldGenericType(field);

            if (this.dataAdapters.hasDataAdapter(fieldGenericType)) {
                parsedVal = this.handleCustomTypeField(fieldGenericType, field, request);
            } else if (field.getType() == UploadedFile.class) {
                parsedVal = this.handleMultipartField(field, request);
            } else if (field.getType() == List.class) {
                parsedVal = this.handleListField(field, request);
            } else {
                parsedVal = this.handlePrimitiveField(field, request);
            }

            try {
                field.set(bindingModel, parsedVal);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the data adapter from {@link ConvertedBy} annotation if present.
     * Else gets the data adapter from the dataAdapters map by the field's generic type.
     * Reloads the data resolver if needed.
     * Returns null if no converter is found.
     * Returns the result of the data adapter.
     */
    private Object handleCustomTypeField(String fieldGenericType, Field field, HttpSoletRequest request) {
        DataAdapter dataAdapter = null;

        if (field.isAnnotationPresent(ConvertedBy.class)) {
            final Class<? extends DataAdapter> convertedClass = field.getAnnotation(ConvertedBy.class).value();
            dataAdapter = this.dataAdapters.getDataAdapter(fieldGenericType, convertedClass);
        } else {
            dataAdapter = this.dataAdapters.getDataAdapter(fieldGenericType);
        }

        if (dataAdapter == null) {
            return null;
        }

        return dataAdapter.resolve(field.getName(), request);
    }

    /**
     * Checks if the request contains a memory file with the same name as the field name.
     * Returns a {@link UploadedFile} if present or otherwise returns null.
     */
    private UploadedFile handleMultipartField(Field field, HttpSoletRequest request) {
        final MultipartFile multipartFile = request.getMultipartFiles().stream()
                .filter(mf -> mf.getFieldName().equalsIgnoreCase(field.getName()))
                .findFirst().orElse(null);

        if (multipartFile != null) {
            return new UploadedFileImpl(this.getAssetsDir(), multipartFile);
        }

        return null;
    }

    /**
     * Calls dataResolver to convert the body parameter that matches the field name.
     */
    private Object handlePrimitiveField(Field field, HttpSoletRequest request) {
        return this.dataResolver.resolve(field.getType(), request.get(field.getName()));
    }

    /**
     * Returns empty list if the value in the body parameters is null for that field.
     * Tries to get the generic type of the list or defaults to String.
     * Gets the list variant for that fieldName and for each available value
     * calls dataResolver to convert the String to the desired type.
     * Returns the list of converted values.
     */
    private List<Object> handleListField(Field field, HttpSoletRequest request) {
        final List<Object> parsedParams = new ArrayList<>();
        if (request.getBodyParametersAsList().get(field.getName()) == null) return parsedParams;

        Class<?> fieldGenericType = String.class;
        try {
            fieldGenericType = ((Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
        } catch (ClassCastException ignored) {
        }

        final List<String> paramsForField = request.getBodyParametersAsList().get(field.getName());

        for (String param : paramsForField) {
            final Object parsedParam = this.dataResolver.resolve(fieldGenericType, param);
            parsedParams.add(parsedParam);
        }

        return parsedParams;
    }

    /**
     * Supports {@link ConvertedBy} and standard {@link JsonDeserialize}.
     *
     * @param payload      -
     * @param bindingModel -
     */
    private void populateBindingModelFromJsonPayload(String payload, Object bindingModel) {
        try {
            this.objectMapper.readerForUpdating(bindingModel).readValue(payload);
        } catch (JsonProcessingException e) {
            throw new ObjectBindingException(String.format(
                    "Could not convert json \n %s \n to binding model %s",
                    payload,
                    bindingModel.getClass()
            ), e);
        }
    }

    /**
     * Gets the asset directory of javache web server.
     */
    private String getAssetsDir() {
        if (this.assetsDir == null) {
            this.assetsDir = this.dependencyContainer.getService(SoletConfig.class).getAttribute(SoletConstants.SOLET_CONFIG_ASSETS_DIR) + "";
        }

        return this.assetsDir;
    }
}
