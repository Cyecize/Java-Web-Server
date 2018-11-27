package com.cyecize.http;

import java.util.List;
import java.util.Map;

public interface HttpRequest {

    void setMethod(String method);

    void setRequestURL(String requestUrl);

    void setSession(HttpSession session);

    void addHeader(String header, String value);

    void addBodyParameter(String parameter, String value);

    boolean isResource();

    String getMethod();

    String getRequestURL();

    HttpSession getSession();

    Map<String, String> getHeaders();

    Map<String, String> getQueryParameters();

    Map<String, String> getBodyParameters();

    Map<String, List<String>> getBodyParametersAsList();

    Map<String, HttpCookie> getCookies();

}
