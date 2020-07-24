package com.cyecize.solet;

import com.cyecize.http.HttpRequest;

import java.io.InputStream;
import java.util.Map;

public interface HttpSoletRequest extends HttpRequest {

    void setContextPath(String contextPath);

    String getContextPath();

    String getRelativeRequestURL();
}
