package com.cyecize.javache.api;

import java.util.HashMap;
import java.util.Map;

public class RequestHandlerSharedData {

    private final Map<String, Object> storage;

    public RequestHandlerSharedData() {
        this.storage = new HashMap<>();
    }

    public Object getObject(String key) {
        return this.storage.get(key);
    }

    public <T> T getObject(String key, Class<T> paramType) {
        return (T) this.storage.get(key);
    }

    public boolean addObject(String key, Object object) {
        if (this.storage.containsKey(key)) {
            return false;
        }

        this.storage.put(key, object);

        return true;
    }
}
