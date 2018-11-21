package com.cyecize.summer.common.models;

import org.jtwig.JtwigModel;

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
}