package com.cyecize.summer.areas.routing.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathFormatter {
    private static final String PATH_PARAMETER_PATTERN = "\\{([a-zA-Z]+)\\}";

    private static final String PATH_PARSED_PARAMETER_PATTERN = "(?<group-name>[a-zA-Z0-9_-]+)";

    public PathFormatter() {

    }

    /**
     * Replaces all {varName} instances in a route with a more suitable regex for matching
     * and extracting PathVariables (?<varName>[a-zA-Z0-9_-]+)
     */
    public String formatPath(String path) {
        final Pattern pathParameterPattern = Pattern.compile(PATH_PARAMETER_PATTERN);
        final Matcher pathParameterMatcher = pathParameterPattern.matcher(path);

        String formatterPath = path;

        while (pathParameterMatcher.find()) {
            String parameterName = pathParameterMatcher.group(1);

            String formattedParameterPattern = PATH_PARSED_PARAMETER_PATTERN.replace("group-name", parameterName);

            formatterPath = formatterPath.replaceFirst(PATH_PARAMETER_PATTERN, formattedParameterPattern);
        }
        if (formatterPath.endsWith("/")) {
            formatterPath += "?";
        } else {
            formatterPath += "/?";
        }

        return "^" + formatterPath + "$";
    }
}
