package com.cyecize.summer.areas.startup.util;

import com.cyecize.ioc.config.MagicConfiguration;
import com.cyecize.ioc.events.ServiceDetailsCreated;
import com.cyecize.ioc.handlers.DependencyResolver;
import com.cyecize.solet.HttpSolet;
import com.cyecize.solet.SoletConfigImpl;
import com.cyecize.summer.areas.security.interceptors.SecurityInterceptor;
import com.cyecize.summer.areas.security.models.Principal;
import com.cyecize.summer.areas.validation.constraints.FieldMatchConstraint;
import com.cyecize.summer.areas.validation.constraints.MaxConstraint;
import com.cyecize.summer.areas.validation.constraints.MaxLengthConstraint;
import com.cyecize.summer.areas.validation.constraints.MediaTypeConstraint;
import com.cyecize.summer.areas.validation.constraints.MinConstraint;
import com.cyecize.summer.areas.validation.constraints.MinLengthConstraint;
import com.cyecize.summer.areas.validation.constraints.NotEmptyConstraint;
import com.cyecize.summer.areas.validation.constraints.NotNullConstraint;
import com.cyecize.summer.areas.validation.constraints.RegExConstraint;
import com.cyecize.summer.areas.validation.models.BindingResultImpl;
import com.cyecize.summer.areas.validation.models.RedirectedBindingResult;
import com.cyecize.summer.areas.validation.objectmapper.GenericDeserializerConvertedByAnnotation;
import com.cyecize.summer.areas.validation.objectmapper.ObjectMapperBean;
import com.cyecize.summer.areas.validation.services.DataAdapterStorageServiceImpl;
import com.cyecize.summer.common.annotations.BeanConfig;
import com.cyecize.summer.common.annotations.Component;
import com.cyecize.summer.common.models.Model;
import com.cyecize.summer.common.models.ModelAndView;
import com.cyecize.summer.common.models.RedirectAttributes;
import com.cyecize.summer.constants.IocConstants;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class MagicConfigurationProducer {

    private static final Map<Class<?>, Class<? extends Annotation>> ADDITIONAL_CLASSES_FOR_SCANNING = Collections
            .unmodifiableMap(new HashMap<>() {{
                //platform
                put(SoletRequestAndResponseBean.class, BeanConfig.class);
                put(ObjectMapperBean.class, BeanConfig.class);
                put(SoletConfigImpl.class, null);
                put(Model.class, Component.class);
                put(ModelAndView.class, Component.class);
                put(RedirectAttributes.class, Component.class);
                put(Principal.class, Component.class);
                put(BindingResultImpl.class, Component.class);
                put(RedirectedBindingResult.class, Component.class);
                put(DataAdapterStorageServiceImpl.class, Component.class);
                put(GenericDeserializerConvertedByAnnotation.class, Component.class);

                //constraints
                put(FieldMatchConstraint.class, Component.class);
                put(MaxConstraint.class, Component.class);
                put(MaxLengthConstraint.class, Component.class);
                put(MediaTypeConstraint.class, Component.class);
                put(MinConstraint.class, Component.class);
                put(MinLengthConstraint.class, Component.class);
                put(NotEmptyConstraint.class, Component.class);
                put(NotNullConstraint.class, Component.class);
                put(RegExConstraint.class, Component.class);

                //interceptors
                put(SecurityInterceptor.class, Component.class);
            }});

    public static MagicConfiguration getConfiguration(Class<? extends HttpSolet> startupSolet,
                                                      Collection<DependencyResolver> dependencyResolvers,
                                                      Collection<ServiceDetailsCreated> serviceScannedCallbacks) {

        final MagicConfiguration configuration = new MagicConfiguration()
                .scanning()
                .addCustomServiceAnnotations(IocConstants.SERVICE_ANNOTATIONS)
                .addAdditionalClassesForScanning(ADDITIONAL_CLASSES_FOR_SCANNING)
                .setClassLoader(startupSolet.getClassLoader())
                .and();

        for (DependencyResolver dependencyResolver : dependencyResolvers) {
            configuration.instantiations().addDependencyResolver(dependencyResolver);
        }

        for (ServiceDetailsCreated serviceScannedCallback : serviceScannedCallbacks) {
            configuration.scanning().addServiceDetailsCreatedCallback(serviceScannedCallback);
        }

        return configuration.build();
    }
}
