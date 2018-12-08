package com.cyecize.summer.areas.template.functions;

import com.cyecize.summer.areas.scanning.services.DependencyContainer;
import com.cyecize.summer.areas.security.models.Principal;
import org.jtwig.exceptions.JtwigException;
import org.jtwig.functions.FunctionRequest;
import org.jtwig.functions.SimpleJtwigFunction;

public class JTwigHasRoleFunction extends SimpleJtwigFunction {

    private static final String INVALID_PARAMETER_MESSAGE = "HasRole function accepts one parameter of type String";

    private final DependencyContainer dependencyContainer;

    public JTwigHasRoleFunction(DependencyContainer dependencyContainer) {
        this.dependencyContainer = dependencyContainer;
    }

    @Override
    public String name() {
        return "hasRole";
    }

    /**
     * If the there is more or less than 1 parameter of the parameters is not String, throw JtwigException
     * Else get the principal from the dependency container and check if a user is present
     * and if the user has the given role.
     */
    @Override
    public Object execute(FunctionRequest functionRequest) {
        if (functionRequest.getArguments().size() != 1 || !(functionRequest.get(0) instanceof String)) {
            throw new JtwigException(INVALID_PARAMETER_MESSAGE);
        }
        Principal principal = this.dependencyContainer.getObject(Principal.class);
        return principal.isUserPresent() && principal.hasAuthority((String) functionRequest.get(0));
    }
}
