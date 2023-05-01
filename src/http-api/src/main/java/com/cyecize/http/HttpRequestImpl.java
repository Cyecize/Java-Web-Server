package com.cyecize.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequestImpl implements HttpRequest {

    private String method;

    private String requestURL;

    private HttpSession session;

    private int contentLength;

    private String remoteAddress;

    private final List<MultipartFile> multipartFiles;

    private final Map<String, String> headers;

    private final Map<String, String> queryParameters;

    private final Map<String, String> bodyParameters;

    private final Map<String, List<String>> bodyParametersAsList;

    private final Map<String, HttpCookie> cookies;

    public HttpRequestImpl() {
        this.multipartFiles = new ArrayList<>();
        this.headers = new HashMap<>();
        this.queryParameters = new HashMap<>();
        this.bodyParameters = new HashMap<>();
        this.bodyParametersAsList = new HashMap<>();
        this.cookies = new HashMap<>();
    }

    @Override
    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public void setRequestURL(String requestUrl) {
        this.requestURL = requestUrl;
    }

    @Override
    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    @Override
    public void setRemoteAddress(String address) {
        this.remoteAddress = address;
    }

    @Override
    public void setSession(HttpSession session) {
        this.session = session;
    }

    @Override
    public void addHeader(String header, String value) {
        this.headers.put(header, value);
    }

    @Override
    public void addBodyParameter(String parameter, String value) {
        this.bodyParameters.put(parameter, value);

        if (!this.bodyParametersAsList.containsKey(parameter)) {
            this.bodyParametersAsList.put(parameter, new ArrayList<>());
        }

        this.bodyParametersAsList.get(parameter).add(value);
    }

    @Override
    public void addMultipartFile(MultipartFile multipartFile) {
        this.multipartFiles.add(multipartFile);
    }

    @Override
    public boolean isResource() {
        return this.getRequestURL().contains(".");
    }

    @Override
    public int getContentLength() {
        return this.contentLength;
    }

    @Override
    public String getRemoteAddress() {
        return this.remoteAddress;
    }

    @Override
    public String getMethod() {
        return this.method;
    }

    @Override
    public String getRequestURL() {
        return this.requestURL;
    }

    @Override
    public String getHost() {
        return this.getHeaders().getOrDefault("Host", "");
    }

    @Override
    public String getRequestURI() {
        return this.getHost() + this.getRequestURL();
    }

    @Override
    public String getContentType() {
        return this.getHeader("Content-Type");
    }

    @Override
    public String getQueryParam(String paramName) {
        return this.queryParameters.get(paramName);
    }

    @Override
    public String getBodyParam(String paramName) {
        return this.bodyParameters.get(paramName);
    }

    @Override
    public String get(String paramName) {
        String param = this.getQueryParam(paramName);

        if (param == null) {
            param = this.getBodyParam(paramName);
        }

        return param;
    }

    @Override
    public String getHeader(String headerName) {
        return this.headers.get(headerName);
    }

    @Override
    public HttpSession getSession() {
        return this.session;
    }

    @Override
    public HttpCookie getCookie(String cookieName) {
        return this.cookies.get(cookieName);
    }

    @Override
    public List<MultipartFile> getMultipartFiles() {
        return this.multipartFiles;
    }

    @Override
    public Map<String, String> getHeaders() {
        return this.headers;
    }

    @Override
    public Map<String, String> getQueryParameters() {
        return this.queryParameters;
    }

    @Override
    public Map<String, String> getBodyParameters() {
        return this.bodyParameters;
    }

    @Override
    public Map<String, List<String>> getBodyParametersAsList() {
        return this.bodyParametersAsList;
    }

    @Override
    public Map<String, HttpCookie> getCookies() {
        return this.cookies;
    }
}
