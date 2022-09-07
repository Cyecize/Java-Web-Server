package com.cyecize.solet;

import com.cyecize.http.HttpCookie;
import com.cyecize.http.HttpResponse;
import com.cyecize.http.HttpStatus;

import java.io.OutputStream;
import java.util.Map;

public class HttpSoletResponseImpl implements HttpSoletResponse {

    private final HttpResponse response;

    private final SoletOutputStream soletOutputStream;

    public HttpSoletResponseImpl(HttpResponse response,
                                 OutputStream clientOutputStream) {
        this.response = response;
        this.soletOutputStream = new SoletOutputStream(clientOutputStream, this);
    }

    @Override
    public void sendRedirect(String location) {
        this.response.setStatusCode(HttpStatus.SEE_OTHER);
        this.response.setContent(location);
        this.response.addHeader("Location", location);
    }

    @Override
    public SoletOutputStream getOutputStream() {
        return this.soletOutputStream;
    }

    @Override
    public void setStatusCode(HttpStatus statusCode) {
        this.response.setStatusCode(statusCode);
    }

    @Override
    public void setContent(String content) {
        this.response.setContent(content);
    }

    @Override
    public void setContent(byte[] content) {
        this.response.setContent(content);
    }

    @Override
    public void addHeader(String header, String value) {
        this.response.addHeader(header, value);
    }

    @Override
    public void addCookie(String name, String value) {
        this.response.addCookie(name, value);
    }

    @Override
    public void addCookie(HttpCookie cookie) {
        this.response.addCookie(cookie);
    }

    @Override
    public String getResponse() {
        return this.response.getResponse();
    }

    @Override
    public HttpStatus getStatusCode() {
        return this.response.getStatusCode();
    }

    @Override
    public byte[] getContent() {
        return this.response.getContent();
    }

    @Override
    public byte[] getBytes() {
        return this.response.getBytes();
    }

    @Override
    public Map<String, String> getHeaders() {
        return this.response.getHeaders();
    }
}
