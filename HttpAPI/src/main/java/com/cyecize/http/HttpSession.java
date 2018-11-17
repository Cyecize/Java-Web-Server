package com.cyecize.http;

import java.util.Map;

public interface HttpSession {

    void invalidate();

    void addAttribute(String name, Object attribute);

    boolean isValid();

    String getId();

    Object getAttribute(String key);

    Map<String, Object> getAttributes();

}
