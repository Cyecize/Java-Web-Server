package com.cyecize.http;

import java.util.HashMap;
import java.util.Map;

public interface HttpSession {

    void invalidate();

    void addAttribute(String name, Object attribute);

    boolean isValid();

    String getId();

    Map<String, Object> getAttributes();

}
