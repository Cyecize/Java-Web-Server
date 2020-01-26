package com.cyecize.summer.areas.startup.util;

import com.cyecize.solet.SoletConfig;
import com.cyecize.solet.SoletConstants;

import java.lang.reflect.Method;
import java.util.Map;

public final class JavacheConfigServiceExtractor {

    public static Map<String, Object> getConfigParams(SoletConfig soletConfig) {
        final Object configService = soletConfig.getAttribute(SoletConstants.SOLET_CONFIG_SERVER_CONFIG_SERVICE_KEY);

        try {
            final Method method = configService.getClass().getMethod("getConfigParams");
            return (Map<String, Object>) method.invoke(configService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
