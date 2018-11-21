package com.cyecize.summer.common.models;

import com.google.common.base.Optional;
import org.jtwig.JtwigModel;
import org.jtwig.reflection.model.Value;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    public Optional<Value> getAttribute(String name) {
        return super.get(name);
    }
}