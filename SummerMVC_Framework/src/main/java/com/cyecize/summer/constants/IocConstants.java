package com.cyecize.summer.constants;

import com.cyecize.summer.areas.template.annotations.TemplateService;
import com.cyecize.summer.common.annotations.BeanConfig;
import com.cyecize.summer.common.annotations.Component;
import com.cyecize.summer.common.annotations.Controller;
import com.cyecize.summer.common.annotations.Service;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;

public class IocConstants {

    public static final Collection<Class<? extends Annotation>> SERVICE_ANNOTATIONS = Arrays.asList(
            TemplateService.class,
            BeanConfig.class,
            Component.class,
            Controller.class,
            Service.class
    );

    public static final String SERVICE_ANNOTATION_LIFESPAN_METHOD_NAME = "lifespan";

    public static final String SOLET_CFG_SCANNED_OBJECTS = "scannedObjects";

    public static final String SOLET_CFG_ASSETS_DIR = "assetsDirectory";

    public static final String COMPONENT_MAP_INTERCEPTORS = "Interceptors";

    public static final String COMPONENT_MAP_VALIDATORS = "Validators";

    public static final String COMPONENT_MAP_DATA_ADAPTERS = "DataAdapters";

}
