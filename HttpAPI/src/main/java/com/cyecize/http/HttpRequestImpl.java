package com.cyecize.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequestImpl implements HttpRequest {

    private String method;

    private String requestURL;

    private HttpSession session;

    private Map<String, String> headers;

    private Map<String, String> queryParameters;

    private Map<String, String> bodyParameters;

    private Map<String, HttpCookie> cookies;

    public HttpRequestImpl(String requestContent) {
        this.initMethod(requestContent);
        this.initRequestUrl(requestContent);
        this.initHeaders(requestContent);
        this.initQueryParameters(requestContent);
        this.initBodyParameters(requestContent);
        this.initCookies();
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
    }

    @Override
    public boolean isResource() {
        return this.getRequestURL().contains(".");
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
    public HttpSession getSession() {
        return this.session;
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
    public Map<String, HttpCookie> getCookies() {
        return this.cookies;
    }

    private void initMethod(String requestContent) {
        this.setMethod(requestContent.split("\\s")[0]);
    }

    private void initRequestUrl(String requestContent) {
        this.setRequestURL(requestContent.split("[\\s\\?]")[1]);
    }

    private void initHeaders(String requestContent) {
        this.headers = new HashMap<>();

        List<String> requestParams = Arrays.asList(
                requestContent.split("\\r\\n"));

        int i = 1;
        while (i < requestParams.size() && requestParams.get(i).length() > 0) {
            String[] headerKeyValuePair = requestParams.get(i).split("\\:\\s");
            this.addHeader(headerKeyValuePair[0], headerKeyValuePair[1]);
            i++;
        }
    }

    private void initQueryParameters(String requestContent) {
        this.queryParameters = new HashMap<>();

        String fullRequestUrl = requestContent.split("[\\s]")[1];

        if (fullRequestUrl.split("\\?").length < 2) {
            return;
        }

        String queryString = fullRequestUrl.split("\\?")[1];
        String[] queryKeyValuePairs = queryString.split("\\&");

        for (int i = 0; i < queryKeyValuePairs.length; i++) {
            String[] queryKeyValuePair = queryKeyValuePairs[i].split("\\=");

            String queryParameterKey = this.decode(queryKeyValuePair[0]);
            String queryParameterValue = queryKeyValuePair.length > 1 ? this.decode(queryKeyValuePair[1]) : null;

            this.queryParameters.putIfAbsent(queryParameterKey, queryParameterValue);
        }
    }

    private void initBodyParameters(String requestContent) {
        if (this.getMethod().equals("POST")) {
            this.bodyParameters = new HashMap<>();

            List<String> requestParams = Arrays.asList(requestContent.split("\\r\\n"));

            if (requestParams.size() > this.headers.size() + 2) {
                List<String> bodyParams = Arrays.asList(requestParams.get(this.headers.size() + 2).split("\\&"));

                for (int i = 0; i < bodyParams.size(); i++) {
                    String[] bodyKeyValuePair = bodyParams.get(i).split("\\=");
                    this.addBodyParameter(this.decode(bodyKeyValuePair[0]), bodyKeyValuePair.length > 1 ? this.decode(bodyKeyValuePair[1]) : null);
                }
            }
        }
    }

    private void initCookies() {
        this.cookies = new HashMap<>();

        if (!this.headers.containsKey("Cookie")) {
            return;
        }

        String cookiesHeader = this.headers.get("Cookie");
        String[] allCookies = cookiesHeader.split("\\;\\s");

        for (int i = 0; i < allCookies.length; i++) {
            String[] cookieNameValuePair = allCookies[i].split("\\=");

            this.cookies.putIfAbsent(cookieNameValuePair[0], new HttpCookieImpl(cookieNameValuePair[0], cookieNameValuePair[1]));
        }
    }

    private String decode(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

}
