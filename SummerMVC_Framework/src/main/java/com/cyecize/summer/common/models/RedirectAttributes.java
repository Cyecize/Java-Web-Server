package com.cyecize.summer.common.models;

import java.util.HashMap;
import java.util.Map;

public class RedirectAttributes {

    private Map<String, Object> attributes;

    public RedirectAttributes() {
        this.attributes = new HashMap<>();
    }

    public void addAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public boolean hasAttribute(String name) {
        return this.attributes.containsKey(name);
    }

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public Map<String, Object> getAttributes() {
        return this.attributes;
    }
}
