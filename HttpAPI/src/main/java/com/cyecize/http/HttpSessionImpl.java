package com.cyecize.http;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HttpSessionImpl implements HttpSession {

    private final Map<String, Object> sessionAttributes;

    private final String sessionId;

    private boolean isSessionValid;

    public HttpSessionImpl(){
        this.isSessionValid = true;
        this.sessionId = UUID.randomUUID().toString();
        this.sessionAttributes = new HashMap<>();
    }

    @Override
    public void invalidate() {
        this.isSessionValid = false;
        this.sessionAttributes.clear();
    }

    @Override
    public void addAttribute(String name, Object attribute) {
        this.sessionAttributes.put(name, attribute);
    }

    @Override
    public boolean isValid() {
        return this.isSessionValid;
    }

    @Override
    public String getId() {
        return this.sessionId;
    }

    @Override
    public Object getAttribute(String key) {
        return this.sessionAttributes.get(key);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.sessionAttributes;
    }
}
