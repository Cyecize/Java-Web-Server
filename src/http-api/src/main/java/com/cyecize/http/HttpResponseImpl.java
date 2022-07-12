package com.cyecize.http;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.cyecize.http.Constants.LINE_SEPARATOR;

public class HttpResponseImpl implements HttpResponse {

    private static final String CONTENT_TYPE = "Content-Type";

    private HttpStatus statusCode;

    private byte[] content;

    private final Map<String, String> headers;

    private final Map<String, HttpCookie> cookies;

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
        final byte[] headers = this.getHeaderString().getBytes();
        final byte[] result = new byte[headers.length + this.getContent().length];

        System.arraycopy(headers, 0, result, 0, headers.length);
        System.arraycopy(this.getContent(), 0, result, headers.length, this.getContent().length);

        return result;
    }

    @Override
    public Map<String, String> getHeaders() {
        return this.headers;
    }

    /**
     * Appends all headers to form a valid HTTP header section.
     *
     * @return headers.
     */
    private String getHeaderString() {
        final StringBuilder result = new StringBuilder()
                .append(HttpStatus.getResponseLine(this.getStatusCode().getStatusCode()))
                .append(LINE_SEPARATOR);

        this.headers.put(CONTENT_TYPE, this.resolveCharset(this.headers.getOrDefault(CONTENT_TYPE, "text/html")));

        for (Map.Entry<String, String> header : this.getHeaders().entrySet()) {
            result.append(header.getKey()).append(": ").append(header.getValue()).append(LINE_SEPARATOR);
        }

        if (!this.cookies.isEmpty()) {
            for (HttpCookie cookie : this.cookies.values()) {
                result.append("Set-Cookie: ").append(cookie.toRFCString()).append(LINE_SEPARATOR);
            }
        }

        result.append(LINE_SEPARATOR);
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
