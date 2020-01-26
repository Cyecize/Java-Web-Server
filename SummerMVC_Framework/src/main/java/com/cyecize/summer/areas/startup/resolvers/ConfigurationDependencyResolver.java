package com.cyecize.summer.areas.startup.resolvers;

import com.cyecize.ioc.handlers.DependencyResolver;
import com.cyecize.ioc.models.DependencyParam;
import com.cyecize.ioc.utils.AliasFinder;
import com.cyecize.ioc.utils.AnnotationUtils;
import com.cyecize.solet.SoletConfig;
import com.cyecize.summer.areas.startup.exceptions.ConfigurationMissingException;
import com.cyecize.summer.common.annotations.Configuration;

import java.util.Map;

public class ConfigurationDependencyResolver implements DependencyResolver {

    private final SoletConfig soletConfig;

    private final Map<String, Object> javacheConfig;

    public ConfigurationDependencyResolver(SoletConfig soletConfig, Map<String, Object> javacheConfig) {
        this.soletConfig = soletConfig;
        this.javacheConfig = javacheConfig;
    }

    @Override
    public boolean canResolve(DependencyParam dependencyParam) {
        return AliasFinder.isAnnotationPresent(dependencyParam.getAnnotations(), Configuration.class);
    }

    @Override
    public Object resolve(DependencyParam dependencyParam) {
        final String configurationName = AnnotationUtils.getAnnotationValue(
                AliasFinder.getAnnotation(dependencyParam.getAnnotations(), Configuration.class)
        ).toString();

        if (this.soletConfig.hasAttribute(configurationName)) {
            return this.soletConfig.getAttribute(configurationName);
        }

        if (this.javacheConfig.containsKey(configurationName)) {
            return this.javacheConfig.get(configurationName);
        }

        throw new ConfigurationMissingException(String.format(
                "Missing configuration with name '%s' for param with type '%s'!",
                configurationName,
                dependencyParam.getDependencyType().getName()
        ));
    }
}