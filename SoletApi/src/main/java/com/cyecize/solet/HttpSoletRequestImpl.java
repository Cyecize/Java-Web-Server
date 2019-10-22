package com.cyecize.solet;

import com.cyecize.http.HttpCookie;
import com.cyecize.http.HttpRequest;
import com.cyecize.http.HttpSession;

import java.util.List;
import java.util.Map;

public class HttpSoletRequestImpl implements HttpSoletRequest {

    private final HttpRequest request;

    private String contextPath;

    public HttpSoletRequestImpl(HttpRequest request) {
        this.request = request;
        this.setContextPath("");
    }

    @Override
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public String getContextPath() {
        return this.contextPath;
    }

    @Override
    public String getRelativeRequestURL() {
        return this.request.getRequestURL().replaceFirst(this.contextPath, "");
    }

    @Override
    public void setMethod(String method) {
        this.request.setMethod(method);
    }

    @Override
    public void setRequestURL(String requestUrl) {
        this.request.setRequestURL(requestUrl);
    }

    @Override
    public void setSession(HttpSession session) {
        this.request.setSession(session);
    }

    @Override
    public void addHeader(String header, String value) {
        this.request.addHeader(header, value);
    }

    @Override
    public void addBodyParameter(String parameter, String value) {
        this.request.addBodyParameter(parameter, value);
    }

    @Override
    public boolean isResource() {
        return this.request.isResource();
    }

    @Override
    public String getMethod() {
        return this.request.getMethod();
    }

    @Override
    public String getRequestURL() {
        return this.request.getRequestURL();
    }

    @Override
    public String getHost() {
        return this.request.getHost();
    }

    @Override
    public String getRequestURI() {
        return this.request.getRequestURI();
    }

    @Override
    public HttpSession getSession() {
        return this.request.getSession();
    }

    @Override
    public Map<String, String> getHeaders() {
        return this.request.getHeaders();
    }

    @Override
    public Map<String, String> getQueryParameters() {
        return this.request.getQueryParameters();
    }

    @Override
    public Map<String, String> getBodyParameters() {
        return this.request.getBodyParameters();
    }

    @Override
    public Map<String, List<String>> getBodyParametersAsList() {
        return this.request.getBodyParametersAsList();
    }

    @Override
    public Map<String, HttpCookie> getCookies() {
        return this.request.getCookies();
    }
}
