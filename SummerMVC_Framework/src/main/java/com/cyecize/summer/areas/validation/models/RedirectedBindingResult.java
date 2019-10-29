package com.cyecize.summer.areas.validation.models;

import com.cyecize.http.HttpSession;
import com.cyecize.summer.common.annotations.Autowired;
import com.cyecize.summer.common.annotations.Component;
import com.cyecize.summer.common.enums.ServiceLifeSpan;
import com.cyecize.summer.constants.RoutingConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component(lifespan = ServiceLifeSpan.REQUEST)
public class RedirectedBindingResult {
    private List<FieldError> errors;

    public RedirectedBindingResult() {
        this.errors = new ArrayList<>();
    }

    @Autowired
    @SuppressWarnings("unchecked")
    public RedirectedBindingResult(HttpSession session) {
        this();
        List<FieldError> errors = (List<FieldError>) session.getAttribute(RoutingConstants.BINDING_ERRORS_SESSION_ID);
        if (errors != null) {
            this.errors = errors;
        }
    }

    public void addNewError(FieldError fieldError) {
        this.errors.add(fieldError);
    }

    public boolean hasErrors() {
        return this.errors.size() > 0;
    }

    public List<FieldError> getFieldErrors(String field) {
        return this.errors.stream().filter(fe -> fe.getFieldName().equals(field)).collect(Collectors.toList());
    }

    public List<FieldError> getErrors() {
        return this.errors;
    }
}
