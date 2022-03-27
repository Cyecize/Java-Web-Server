package com.cyecize.summer.areas.template.functions;

import com.cyecize.summer.areas.startup.services.DependencyContainer;
import com.cyecize.summer.areas.validation.models.RedirectedBindingResult;
import org.jtwig.exceptions.JtwigException;
import org.jtwig.functions.FunctionRequest;
import org.jtwig.functions.SimpleJtwigFunction;

public class JTwigFieldErrorsFunction extends SimpleJtwigFunction {

    private static final String INVALID_PARAMETER_MESSAGE = "FormErrors function accepts zero or one parameters of type String";

    private final RedirectedBindingResult redirectedBindingResult;

    public JTwigFieldErrorsFunction(RedirectedBindingResult redirectedBindingResult) {
        this.redirectedBindingResult = redirectedBindingResult;
    }

    @Override
    public String name() {
        return "formErrors";
    }

    /**
     * If there are no parameters, return all errors.
     * If there is one parameter of type String, return errors for a given field.
     * Else throw {@link JtwigException}
     */
    @Override
    public Object execute(FunctionRequest functionRequest) {
        if (functionRequest.getNumberOfArguments() < 1) {
            return this.redirectedBindingResult.getErrors();
        }

        if (functionRequest.getNumberOfArguments() > 1 || functionRequest.getArguments().get(0) == null ||
                !(functionRequest.getArguments().get(0) instanceof String)) {
            throw new JtwigException(INVALID_PARAMETER_MESSAGE);
        }

        return this.redirectedBindingResult.getFieldErrors((String) functionRequest.getArguments().get(0));
    }
}
