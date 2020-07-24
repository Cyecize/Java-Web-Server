package com.cyecize.http;

import java.util.Map;

public interface HttpResponse {

    void setStatusCode(HttpStatus statusCode);

    void setContent(String content);

    void setContent(byte[] content);

    void addHeader(String header, String value);

    void addCookie(String name, String value);

    void addCookie(HttpCookie cookie);

    String getResponse();

    HttpStatus getStatusCode();

    byte[] getContent();

    byte[] getBytes();

    Map<String, String> getHeaders();
}
