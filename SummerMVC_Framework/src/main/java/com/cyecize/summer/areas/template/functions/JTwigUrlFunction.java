package com.cyecize.summer.areas.template.functions;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;
import org.jtwig.exceptions.JtwigException;
import org.jtwig.functions.FunctionRequest;
import org.jtwig.functions.SimpleJtwigFunction;

public class JTwigUrlFunction extends SimpleJtwigFunction {

    private static final String INVALID_PARAM_ERROR = "Path function expects one parameter of type string.";

    private final DependencyContainer dependencyContainer;

    public JTwigUrlFunction(DependencyContainer dependencyContainer) {
        this.dependencyContainer = dependencyContainer;
    }

    @Override
    public String name() {
        return "url";
    }

    @Override
    public Object execute(FunctionRequest functionRequest) {
        if (functionRequest.getArguments().size() != 1 || !(functionRequest.get(0) instanceof String)) {
            throw new JtwigException(INVALID_PARAM_ERROR);
        }

        return this.getURI() + functionRequest.get(0);
    }

    /**
     * Gets the Host URL and adds app name prefix if one is present by replacing the relative url with ""
     * so the leftover is the app name prefix.
     */
    private String getURI() {
        final HttpSoletRequest request = this.dependencyContainer.getService(HttpSoletRequest.class);
        String URI = request.getHost() + "/" + request.getRequestURL().replace(request.getRelativeRequestURL(), "");

        if (URI.endsWith("/")) {
            URI = URI.substring(0, URI.length() - 1);
        }
        // prepend // since schema is not present.
        return "//" + URI;
    }
}
