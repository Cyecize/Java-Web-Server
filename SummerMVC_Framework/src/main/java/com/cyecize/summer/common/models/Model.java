package com.cyecize.summer.common.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Model {
    private Map<String, Object> attributes;

    public Model() {
        this.attributes = new HashMap<>();
    }

    public void addAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    public boolean hasAttribute(String name) {
        return this.attributes.containsKey(name);
    }

    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(this.attributes);
    }
}