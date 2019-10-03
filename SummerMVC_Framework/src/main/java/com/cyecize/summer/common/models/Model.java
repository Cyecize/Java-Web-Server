package com.cyecize.summer.common.models;

import com.cyecize.http.HttpSession;
import com.cyecize.summer.common.annotations.Autowired;
import com.cyecize.summer.common.annotations.Component;
import com.cyecize.summer.common.enums.ServiceLifeSpan;
import com.cyecize.summer.constants.RoutingConstants;
import org.jtwig.JtwigModel;

import java.util.Map;

@Component(lifespan = ServiceLifeSpan.REQUEST)
public class Model extends JtwigModel {

    public Model() {
        super();
    }

    @Autowired
    public Model(HttpSession session) {
        Map<String, Object> attributes = (Map<String, Object>) session.getAttribute(RoutingConstants.REDIRECT_ATTRIBUTES_SESSION_ID);
        if (attributes != null) {
            this.populateModel(attributes);
        }
    }

    public void addAttribute(String name, Object value) {
        super.with(name, value);
    }

    public boolean hasAttribute(String name) {
        return super.get(name).isPresent();
    }

    public Object getAttribute(String name) {
        if (super.get(name).isPresent()) {
            return super.get(name).get().getValue();
        }
        return null;
    }

    private void populateModel(Map<String, Object> attributes) {
        attributes.forEach(this::addAttribute);
    }
}