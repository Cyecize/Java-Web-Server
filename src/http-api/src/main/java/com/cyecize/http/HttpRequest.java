package com.cyecize.http;

import java.util.List;
import java.util.Map;

public interface HttpRequest {

    void setMethod(String method);

    void setRequestURL(String requestUrl);

    void setContentLength(int contentLength);

    void setRemoteAddress(String address);

    void setSession(HttpSession session);

    void addHeader(String header, String value);

    void addBodyParameter(String parameter, String value);

    void addMultipartFile(MultipartFile multipartFile);

    boolean isResource();

    int getContentLength();

    String getRemoteAddress();

    String getMethod();

    String getRequestURL();

    String getHost();

    String getRequestURI();

    String getContentType();

    String getQueryParam(String paramName);

    String getBodyParam(String paramName);

    String get(String paramName);

    String getHeader(String headerName);

    HttpSession getSession();

    HttpCookie getCookie(String cookieName);

    List<MultipartFile> getMultipartFiles();

    Map<String, String> getHeaders();

    Map<String, String> getQueryParameters();

    Map<String, String> getBodyParameters();

    Map<String, List<String>> getBodyParametersAsList();

    Map<String, HttpCookie> getCookies();
}
