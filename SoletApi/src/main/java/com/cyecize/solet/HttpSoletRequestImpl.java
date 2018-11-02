package com.cyecize.solet;

import com.cyecize.http.HttpRequestImpl;

import java.io.InputStream;

public class HttpSoletRequestImpl extends HttpRequestImpl implements HttpSoletRequest {
    private InputStream inputStream;

    public HttpSoletRequestImpl(String requestContent, InputStream requestStream) {
        super(requestContent);
        this.setInputStream(inputStream);
    }

    @Override
    public InputStream getInputStream() {
        return this.inputStream;
    }

    private void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
