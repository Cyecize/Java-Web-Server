package com.cyecize.summer.areas.validation.services;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.MemoryFile;
import com.cyecize.solet.SoletConfig;
import com.cyecize.summer.areas.routing.interfaces.MultipartFile;
import com.cyecize.summer.areas.routing.models.MultipartFileImpl;
import com.cyecize.summer.areas.routing.utils.PrimitiveTypeDataResolver;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;
import com.cyecize.summer.areas.validation.interfaces.DataAdapter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

import static com.cyecize.summer.constants.IocConstants.*;

public class ObjectBindingServiceImpl implements ObjectBindingService {

    private static final String NO_GENERIC_TYPE_FOUND_FOR_CLS_FORMAT = "No generic type found for data adapter \"%s\".";

    private final DependencyContainer dependencyContainer;

    private Map<String, DataAdapter> dataAdapters;

    private PrimitiveTypeDataResolver dataResolver;

    private String assetsDir;

    public ObjectBindingServiceImpl(DependencyContainer dependencyContainer, Set<Object> dataAdapters) {
        this.dependencyContainer = dependencyContainer;
        this.setDataAdapters(dataAdapters);
        this.dataResolver = new PrimitiveTypeDataResolver();
    }

    /**
     * Iterates through fields and for every field checks if there is a data adapter for it.
     * If there is, use the data adapter, otherwise check if the field is MultipartFile or List.
     * Finally consider that the field is primitive.
     */
    @Override
    public void populateBindingModel(Object bindingModel) {
        HttpSoletRequest request = this.dependencyContainer.getObject(HttpSoletRequest.class);
        if (request.getBodyParameters() == null) {
            return;
        }
        Arrays.stream(bindingModel.getClass().getDeclaredFields()).forEach(field -> {
            field.setAccessible(true);

            Object parsedVal;
            //add other types here
            String fieldGenericType = this.getFieldGenericType(field);
            if (this.dataAdapters.containsKey(this.getFieldGenericType(field))) {
                parsedVal = this.dataAdapters.get(fieldGenericType).resolveField(field, request);
            } else if (field.getType() == MultipartFile.class) {
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
        });
    }

    private String getFieldGenericType(Field field) {
        return field.getGenericType().getTypeName();
    }

    private MultipartFile handleMultipartField(Field field, HttpSoletRequest request) {
        MemoryFile memoryFile = request.getUploadedFiles().get(field.getName());
        if (memoryFile != null) {
            return new MultipartFileImpl(this.getAssetsDir(), memoryFile);
        }
        return null;
    }

    private Object handlePrimitiveField(Field field, HttpSoletRequest request) {
        return this.dataResolver.resolve(field.getType(), request.getBodyParameters().get(field.getName()));
    }

    private List<Object> handleListField(Field field, HttpSoletRequest request) {
        List<Object> parsedParams = new ArrayList<>();
        if (request.getBodyParametersAsList().get(field.getName()) == null) return parsedParams;

        Class<?> fieldGenericType = String.class;
        try {
            fieldGenericType = ((Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
        } catch (ClassCastException ignored) {
        }

        List<String> paramsForField = request.getBodyParametersAsList().get(field.getName());

        for (String param : paramsForField) {
            Object parsedParam = this.dataResolver.resolve(fieldGenericType, param);
            parsedParams.add(parsedParam);
        }

        return parsedParams;
    }

    private String getAssetsDir() {
        if (this.assetsDir == null) {
            this.assetsDir = this.dependencyContainer.getObject(SoletConfig.class).getAttribute(SOLET_CFG_ASSETS_DIR) + "";
        }
        return this.assetsDir;
    }

    private void setDataAdapters(Set<Object> adapters) {
        this.dataAdapters = new HashMap<>();
        for (Object adapter : adapters) {
            try {
                String genericType = ((ParameterizedType) adapter.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0].getTypeName();
                this.dataAdapters.put(genericType, (DataAdapter) adapter);
            } catch (Throwable e) {
                throw new RuntimeException(NO_GENERIC_TYPE_FOUND_FOR_CLS_FORMAT, e);
            }
        }
    }
}
