package com.cyecize.summer.areas.startup.resolvers;

import com.cyecize.ioc.handlers.DependencyResolver;
import com.cyecize.ioc.models.DependencyParam;
import com.cyecize.solet.SoletConfig;
import com.cyecize.solet.SoletConstants;
import com.cyecize.solet.SoletLogger;

public class SoletLoggerDependencyResolver implements DependencyResolver {

    private final SoletConfig soletConfig;

    public SoletLoggerDependencyResolver(SoletConfig soletConfig) {
        this.soletConfig = soletConfig;
    }

    @Override
    public boolean canResolve(DependencyParam dependencyParam) {
        return dependencyParam.getDependencyType().isAssignableFrom(SoletLogger.class);
    }

    @Override
    public Object resolve(DependencyParam dependencyParam) {
        return this.soletConfig.getAttribute(SoletConstants.SOLET_CONFIG_LOGGER);
    }
}
