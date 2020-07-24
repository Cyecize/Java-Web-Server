package com.cyecize.javache.common;

import java.io.File;

public final class PathUtils {

    public static String appendPath(String url, String path) {
        url = trimEndingSlashes(url);
        path = trimAllSlashes(path);

        return url + File.separator + path;
    }

    public static String trimEndingSlashes(String str) {
        if (str.endsWith("/") || str.endsWith("\\")) {
            return str.substring(0, str.length() - 1);
        }

        return str;
    }

    public static String trimBeginningSlashes(String str) {
        if (str.startsWith("/") || str.startsWith("\\")) {
            return str.substring(1);
        }

        return str;
    }

    public static String trimAllSlashes(String str) {
        return trimBeginningSlashes(trimEndingSlashes(str));
    }
}
