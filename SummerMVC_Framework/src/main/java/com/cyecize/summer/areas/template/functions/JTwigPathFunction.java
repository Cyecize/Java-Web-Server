package com.cyecize.summer.areas.template.functions;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;
import org.jtwig.exceptions.JtwigException;
import org.jtwig.functions.FunctionRequest;
import org.jtwig.functions.SimpleJtwigFunction;

public class JTwigPathFunction extends SimpleJtwigFunction {

    private static final String INVALID_PARAM_ERROR = "Path function expects one parameter of type string.";

    private final DependencyContainer dependencyContainer;

    public JTwigPathFunction(DependencyContainer dependencyContainer) {
        this.dependencyContainer = dependencyContainer;
    }

    @Override
    public String name() {
        return "path";
    }

    @Override
    public Object execute(FunctionRequest functionRequest) {
        if (functionRequest.getArguments().size() != 1 || !(functionRequest.get(0) instanceof String)) {
            throw new JtwigException(INVALID_PARAM_ERROR);
        }

        return this.createPrefix() + functionRequest.get(0);
    }

    /**
     * Gets app name by replacing the relative URL with "" on the absolute URL.
     */
    private String createPrefix() {
        final HttpSoletRequest request = this.dependencyContainer.getService(HttpSoletRequest.class);
        String appName = request.getRequestURL().replace(request.getRelativeRequestURL(), "");

        if (appName.length() < 1) {
            return appName;
        }

        if (appName.endsWith("/")) {
            appName = appName.substring(0, appName.length() - 1);
        }

        if (!appName.startsWith("/")) {
            appName = "/" + appName;
        }

        return appName;
    }
}
