package com.cyecize.solet;

import java.util.Map;

public interface SoletConfig {

    void setAttribute(String name, Object attribute);

    void deleteAttribute(String name);

    Object getAttribute(String name);

    Map<String, Object> getAllAttributes();

}
