package com.cyecize.summer.areas.template.functions;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.summer.areas.startup.services.DependencyContainer;
import org.jtwig.exceptions.JtwigException;
import org.jtwig.functions.FunctionRequest;
import org.jtwig.functions.SimpleJtwigFunction;

public class JTwigUrlFunction extends SimpleJtwigFunction {

    private static final String INVALID_PARAM_ERROR = "Path function expects one parameter of type string.";

    private final HttpSoletRequest request;

    public JTwigUrlFunction(HttpSoletRequest httpSoletRequest) {
        this.request = httpSoletRequest;
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
        String URI = this.request.getHost() + "/" + this.request.getRequestURL()
                .replace(this.request.getRelativeRequestURL(), "");

        if (URI.endsWith("/")) {
            URI = URI.substring(0, URI.length() - 1);
        }
        // prepend // since schema is not present.
        return "//" + URI;
    }
}
