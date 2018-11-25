package com.cyecize.summer.areas.validation.services;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.MemoryFile;
import com.cyecize.solet.SoletConfig;
import com.cyecize.summer.areas.routing.interfaces.MultipartFile;
import com.cyecize.summer.areas.routing.models.MultipartFileImpl;
import com.cyecize.summer.areas.routing.utils.PrimitiveTypeDataResolver;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;

import java.lang.reflect.Field;
import java.util.Arrays;

import static com.cyecize.summer.constants.IocConstants.*;

public class ObjectBindingServiceImpl implements ObjectBindingService {

    private final DependencyContainer dependencyContainer;

    private PrimitiveTypeDataResolver dataResolver;

    private String assetsDir;

    public ObjectBindingServiceImpl(DependencyContainer dependencyContainer) {
        this.dependencyContainer = dependencyContainer;
        this.dataResolver = new PrimitiveTypeDataResolver();
    }

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
            if (field.getType() == MultipartFile.class) {
                parsedVal = this.handleMultipartField(field, request);
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

    private String getAssetsDir() {
        if (this.assetsDir == null) {
            this.assetsDir = this.dependencyContainer.getObject(SoletConfig.class).getAttribute(SOLET_CFG_ASSETS_DIR) + "";
        }
        return this.assetsDir;
    }
}
