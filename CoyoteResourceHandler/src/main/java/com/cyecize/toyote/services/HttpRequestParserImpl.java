package com.cyecize.toyote.services;

import com.cyecize.http.HttpCookieImpl;
import com.cyecize.http.HttpRequest;
import com.cyecize.http.HttpRequestImpl;
import com.cyecize.ioc.annotations.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class HttpRequestParserImpl implements HttpRequestParser {

    private static final String CONTENT_LENGTH_HEADER_NAME = "Content-Length";

    private static final String COOKIE_HEADER_NAME = "Cookie";

    @Override
    public HttpRequest parseHttpRequest(InputStream inputStream) throws IOException {
        final HttpRequest request = new HttpRequestImpl();

        final List<String> headers = parseMetadataLines(inputStream, false);

        setMethodAndURL(headers.get(0), request);
        addQueryParameters(headers.get(0), request);
        addHeaders(headers, request);
        initCookies(request);

        //TODO check for different content types
        setBodyParameters(readBody(inputStream, request), request);

        return request;
    }

    private static List<String> parseMetadataLines(InputStream inputStream, boolean allowNewLineWithoutReturn) throws IOException {
        final List<String> metadataLines = new ArrayList<>();

        StringBuilder metadataBuilder = new StringBuilder();
        boolean wasNewLine = true;
        int lineNumber = 1;
        int b;

        while ((b = inputStream.read()) >= 0) {
            if (b == '\r') {
                // expect new-line
                int next = inputStream.read();
                if (next < 0 || next == '\n') {
                    lineNumber++;
                    if (wasNewLine) break;
                    metadataLines.add(metadataBuilder.toString());
                    if (next < 0) break;
                    metadataBuilder = new StringBuilder();
                    wasNewLine = true;
                } else {
                    inputStream.close();
                    throw new IOException(String.format("Illegal character after return on line %d.", lineNumber));
                }
            } else if (b == '\n') {
                if (!allowNewLineWithoutReturn) {
                    throw new IOException(String.format("Illegal new-line character without preceding return on line %d.", lineNumber));
                }

                // unexpected, but let's accept new-line without returns
                lineNumber++;
                if (wasNewLine) break;
                metadataLines.add(metadataBuilder.toString());
                metadataBuilder = new StringBuilder();
                wasNewLine = true;
            } else {
                metadataBuilder.append((char) b);
                wasNewLine = false;
            }
        }

        if (metadataBuilder.length() > 0) {
            metadataLines.add(metadataBuilder.toString());
        }

        return metadataLines;
    }

    private static void setMethodAndURL(String requestFirstLine, HttpRequest request) {
        request.setMethod(requestFirstLine.split("\\s")[0]);
        request.setRequestURL(requestFirstLine.split("[\\s\\?]")[1]);
    }

    private static void addHeaders(List<String> requestMetadata, HttpRequest request) {
        for (int i = 1; i < requestMetadata.size(); i++) {
            final String[] headerKeyValuePair = requestMetadata.get(i).split(":\\s+");
            request.addHeader(headerKeyValuePair[0], headerKeyValuePair[1]);
        }
    }

    private static void addQueryParameters(String requestFirstLine, HttpRequest request) {
        final String fullRequestURL = requestFirstLine.split("\\s")[1];
        final String[] urlQueryParamPair = fullRequestURL.split("\\?");

        if (urlQueryParamPair.length < 2) {
            return;
        }

        final String[] queryParamPairs = urlQueryParamPair[1].split("&");

        final Map<String, String> queryParameters = request.getQueryParameters();

        for (String paramPair : queryParamPairs) {
            final String[] queryParamPair = paramPair.split("=");

            final String keyName = decode(queryParamPair[0]);
            final String value = queryParamPair.length > 1 ? decode(queryParamPair[1]) : null;

            queryParameters.put(keyName, value);
        }
    }

    private static void initCookies(HttpRequest request) {
        if (!request.getHeaders().containsKey(COOKIE_HEADER_NAME)) {
            return;
        }

        final String[] allCookies = request.getHeaders().get(COOKIE_HEADER_NAME).split(";\\s");

        for (String cookieStr : allCookies) {
            final String[] cookieKeyValuePair = cookieStr.split("=");

            final String keyName = decode(cookieKeyValuePair[0]);
            final String value = cookieKeyValuePair.length > 1 ? decode(cookieKeyValuePair[1]) : null;

            request.getCookies().put(keyName, new HttpCookieImpl(keyName, value));
        }
    }

    private static String readBody(InputStream inputStream, HttpRequest request) throws IOException {
        //TODO remove printing
        System.out.println("Available count " + inputStream.available());

        int contentLength = inputStream.available();
        if (request.getHeaders().containsKey(CONTENT_LENGTH_HEADER_NAME)) {
            contentLength = Integer.parseInt(request.getHeaders().get(CONTENT_LENGTH_HEADER_NAME));
            System.out.println("Content Length is " + contentLength);
        }

        byte[] bytes = inputStream.readNBytes(contentLength);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static void setBodyParameters(String requestBody, HttpRequest request) {
        final String[] bodyParamPairs = requestBody.split("&");

        for (String bodyParamPair : bodyParamPairs) {
            String[] tokens = bodyParamPair.split("=");
            final String paramKey = decode(tokens[0]);
            final String value = tokens.length > 1 ? decode(tokens[1]) : null;

            request.addBodyParameter(paramKey, value);
        }
    }

    private static String decode(String str) {
        return URLDecoder.decode(str, StandardCharsets.UTF_8);
    }
}
