package com.cyecize.toyote.utils;

import java.util.HashMap;
import java.util.Map;

public class MultipartUtils {

    public static Map<String, String> parseContentDispositionString(String contentDispositionStr) {
        final Map<String, String> params = new HashMap<>();

        final String[] keyValuePairs = contentDispositionStr.split(";\\s+");

        for (String keyValuePair : keyValuePairs) {
            final String[] kvp = keyValuePair.split("=");
            if (kvp.length < 2) {
                continue;
            }

            final String key = kvp[0];
            final String value = kvp[1].substring(1, kvp[1].length() - 1);

            params.put(key, value);
        }

        return params;
    }
}
