package com.cyecize.http;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponseImpl implements HttpResponse {

    private HttpStatus statusCode;

    private HttpSession session;

    private byte[] content;

    private Map<String, String> headers;

    private Map<String, HttpCookie> cookies;

    public HttpResponseImpl() {
        this.setContent(new byte[0]);
        this.headers = new HashMap<>();
        this.cookies = new HashMap<>();
    }

    @Override
    public void setStatusCode(HttpStatus statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public void addHeader(String header, String value) {
        this.headers.put(header, value);
    }

    @Override
    public void addCookie(String name, String value) {
        this.cookies.put(name, new HttpCookieImpl(name, value));
    }

    @Override
    public void addCookie(HttpCookie cookie) {
        this.cookies.put(cookie.getName(), cookie);
    }

    @Override
    public byte[] getContent() {
        return this.content;
    }

    @Override
    public byte[] getBytes() {
        byte[] headersBytes = this.getHeaderBytes();
        byte[] bodyBytes = this.getContent();

        byte[] fullResponse = new byte[headersBytes.length + bodyBytes.length];

        System.arraycopy(headersBytes, 0, fullResponse, 0, headersBytes.length);
        System.arraycopy(bodyBytes, 0, fullResponse, headersBytes.length, bodyBytes.length);

        return fullResponse;
    }

    @Override
    public Map<String, String> getHeaders() {
        return this.headers;
    }

    @Override
    public HttpStatus getStatusCode() {
        return this.statusCode;
    }

    private byte[] getHeaderBytes() {
        StringBuilder result = new StringBuilder()
                .append(ResponseLines.getResponseLine(this.getStatusCode().getStatusCode())).append(System.lineSeparator());

        for (Map.Entry<String, String> header : this.getHeaders().entrySet()) {
            result.append(header.getKey()).append(": ").append(header.getValue()).append(System.lineSeparator());
        }

        if (!this.cookies.isEmpty()) {
            for (HttpCookie cookie : this.cookies.values()) {
                result.append("Set-Cookie: ").append(cookie.toRFCString()).append(System.lineSeparator());
            }
        }

        result.append(System.lineSeparator());
        return result.toString().getBytes(StandardCharsets.UTF_8);
    }
}
