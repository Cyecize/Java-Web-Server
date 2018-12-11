package com.cyecize.http;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponseImpl implements HttpResponse {

    private static final String CONTENT_TYPE = "Content-Type";

    private HttpStatus statusCode;

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
    public void setContent(String content) {
        this.content = content.getBytes(StandardCharsets.UTF_8);
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
    public String getResponse() {
        return this.getHeaderString() + new String(this.getContent(), StandardCharsets.UTF_8);
    }

    @Override
    public HttpStatus getStatusCode() {
        return this.statusCode;
    }

    @Override
    public byte[] getContent() {
        return this.content;
    }

    @Override
    public byte[] getBytes() {
        byte[] headers = this.getHeaderString().getBytes();
        byte[] result = new byte[headers.length + this.getContent().length];
        System.arraycopy(headers, 0, result, 0, headers.length);
        System.arraycopy(this.getContent(), 0, result, headers.length, this.getContent().length);
        return result;
    }

    @Override
    public Map<String, String> getHeaders() {
        return this.headers;
    }


    private String getHeaderString() {
        StringBuilder result = new StringBuilder()
                .append(ResponseLines.getResponseLine(this.getStatusCode().getStatusCode())).append(System.lineSeparator());

        this.headers.put(CONTENT_TYPE, this.resolveCharset(this.headers.getOrDefault(CONTENT_TYPE, "text/html")));

        for (Map.Entry<String, String> header : this.getHeaders().entrySet()) {
            result.append(header.getKey()).append(": ").append(header.getValue()).append(System.lineSeparator());
        }

        if (!this.cookies.isEmpty()) {
            for (HttpCookie cookie : this.cookies.values()) {
                result.append("Set-Cookie: ").append(cookie.toRFCString()).append(System.lineSeparator());
            }
        }

        result.append(System.lineSeparator());
        return result.toString();
    }

    private String resolveCharset(String contentType) {
        if (contentType == null || contentType.contains("charset")) {
            return contentType;
        } else {
            return contentType + "; charset=utf8";
        }
    }
}
