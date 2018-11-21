package com.cyecize.solet;

import com.cyecize.http.HttpRequestImpl;

import java.io.InputStream;

public class HttpSoletRequestImpl extends HttpRequestImpl implements HttpSoletRequest {

    private InputStream inputStream;

    private String contextPath;

    public HttpSoletRequestImpl(String requestContent, InputStream requestStream) {
        super(requestContent);
        this.inputStream = requestStream;
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
        return super.getRequestURL().replaceFirst(this.contextPath, "");
    }

    @Override
    public InputStream getInputStream() {
        return this.inputStream;
    }
}
