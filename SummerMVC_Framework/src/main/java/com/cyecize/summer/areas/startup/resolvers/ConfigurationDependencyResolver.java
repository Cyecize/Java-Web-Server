package com.cyecize.summer.areas.startup.resolvers;

import com.cyecize.ioc.handlers.DependencyResolver;
import com.cyecize.ioc.models.DependencyParam;
import com.cyecize.ioc.utils.AliasFinder;
import com.cyecize.ioc.utils.AnnotationUtils;
import com.cyecize.solet.SoletConfig;
import com.cyecize.summer.areas.routing.utils.PrimitiveTypeDataResolver;
import com.cyecize.summer.areas.startup.exceptions.ConfigurationMissingException;
import com.cyecize.summer.common.annotations.Configuration;

import java.util.Map;

/**
 * Plugin for MagicInjector which helps resolve dependencies with {@link Configuration} annotation.
 */
public class ConfigurationDependencyResolver implements DependencyResolver {

    private final SoletConfig soletConfig;

    private final Map<String, Object> javacheConfig;

    private final Map<String, String> userConfig;

    private final PrimitiveTypeDataResolver dataResolver;

    public ConfigurationDependencyResolver(SoletConfig soletConfig, Map<String, Object> javacheConfig,
                                           Map<String, String> userConfig) {
        this.soletConfig = soletConfig;
        this.javacheConfig = javacheConfig;
        this.userConfig = userConfig;
        this.dataResolver = new PrimitiveTypeDataResolver();
    }

    @Override
    public boolean canResolve(DependencyParam dependencyParam) {
        return AliasFinder.isAnnotationPresent(dependencyParam.getAnnotations(), Configuration.class);
    }

    /**
     * Gets the name of the requested parameter from the {@link Configuration} annotation.
     * Looks for a configuration with that name in {@link SoletConfig}, Javache config, config, specified by the user
     * in the properties file.
     *
     * @return resolved dependency value.
     * @throws ConfigurationMissingException if a config with that name could not be found.
     */
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

        if (this.userConfig.containsKey(configurationName)) {
            return this.dataResolver.resolve(
                    dependencyParam.getDependencyType(),
                    this.userConfig.get(configurationName)
            );
        }

        throw new ConfigurationMissingException(String.format(
                "Missing configuration with name '%s' for param with type '%s'!",
                configurationName,
                dependencyParam.getDependencyType().getName()
        ));
    }
}
