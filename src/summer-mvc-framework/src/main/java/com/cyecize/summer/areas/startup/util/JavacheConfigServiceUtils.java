package com.cyecize.summer.areas.startup.util;

import com.cyecize.solet.SoletConfig;
import com.cyecize.solet.SoletConstants;
import com.cyecize.summer.areas.routing.utils.PrimitiveTypeDataResolver;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public final class JavacheConfigServiceUtils {

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getConfigParams(SoletConfig soletConfig) {
        final Object configService = soletConfig.getAttribute(SoletConstants.SOLET_CONFIG_SERVER_CONFIG_SERVICE_KEY);

        try {
            final Method method = configService.getClass().getMethod("getConfigParams");
            return (Map<String, Object>) method.invoke(configService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void overrideJavacheConfig(Map<String, Object> javacheConfig, Map<String, String> userConfig) {
        final Map<String, Object> overriddenValues = new HashMap<>();
        final PrimitiveTypeDataResolver dataResolver = new PrimitiveTypeDataResolver();

        for (Map.Entry<String, String> userConfigEntry : userConfig.entrySet()) {
            final String configName = userConfigEntry.getKey().toUpperCase();
            if (!javacheConfig.containsKey(configName)) {
                continue;
            }

            final Class<?> configType = javacheConfig.get(configName).getClass();
            overriddenValues.put(configName, dataResolver.resolve(configType, userConfigEntry.getValue()));
        }

        javacheConfig.putAll(overriddenValues);
    }
}
