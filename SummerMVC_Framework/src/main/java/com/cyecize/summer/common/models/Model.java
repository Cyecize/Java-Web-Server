package com.cyecize.summer.common.models;

import org.jtwig.JtwigModel;

public class Model extends JtwigModel {

    public Model() {
        super();
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

}