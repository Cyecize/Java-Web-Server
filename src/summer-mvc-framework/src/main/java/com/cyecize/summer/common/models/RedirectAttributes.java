package com.cyecize.summer.common.models;

import com.cyecize.summer.common.annotations.Autowired;
import com.cyecize.summer.common.annotations.Component;
import com.cyecize.summer.common.enums.ServiceLifeSpan;

import java.util.HashMap;
import java.util.Map;

@Component(lifespan = ServiceLifeSpan.REQUEST)
public class RedirectAttributes {

    private final Map<String, Object> attributes;

    @Autowired
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
