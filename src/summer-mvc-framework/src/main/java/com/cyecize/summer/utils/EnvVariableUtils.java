package com.cyecize.summer.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class EnvVariableUtils {
    public static String getEnvVariable(String key) {
        if (StringUtils.trimToNull(key) == null) {
            return null;
        }

        String val = System.getenv(key);
        if (val == null) {
            val = System.getenv(key.toUpperCase(Locale.ROOT).replace(".", "_"));
        }

        return val;
    }
}
