package com.cyecize.summer.common.models;

import org.jtwig.JtwigModel;

import java.util.Map;

public class Model extends JtwigModel {

    public Model() {
        super();
    }

    public Model(Map<String, Object> attributes) {
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