package com.cyecize.http;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HttpSessionImpl implements HttpSession {

    private boolean isSessionValid;

    private String sessionId;

    private Map<String, Object> sessionAttributes;

    public HttpSessionImpl(){
        this.isSessionValid = true;
        this.sessionId = UUID.randomUUID().toString();
        this.sessionAttributes = new HashMap<>();
    }

    @Override
    public void invalidate() {
        this.isSessionValid = false;
        this.sessionAttributes = null;
        System.gc();
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
    public Map<String, Object> getAttributes() {
        return this.sessionAttributes;
    }
}
