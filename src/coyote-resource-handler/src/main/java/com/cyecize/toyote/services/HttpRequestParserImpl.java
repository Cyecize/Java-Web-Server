package com.cyecize.toyote.services;

import com.cyecize.http.HttpCookieImpl;
import com.cyecize.http.HttpRequest;
import com.cyecize.http.HttpRequestImpl;
import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.api.RequestHandlerSharedData;
import com.cyecize.javache.api.SharedDataPropertyNames;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.javache.services.LoggingService;
import com.cyecize.toyote.ToyoteConstants;
import com.cyecize.toyote.enums.FormDataParserProvider;
import com.cyecize.toyote.exceptions.CannotParseRequestException;
import com.cyecize.toyote.exceptions.RequestTooBigException;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HttpRequestParserImpl implements HttpRequestParser {

    private static final String REQUEST_TOO_BIG_MSG = "Request too big.";

    private final List<FormDataParser> formDataParsers;

    private final Map<FormDataParserProvider, FormDataParser> instanceProviderMap = new HashMap<>();

    private final LoggingService loggingService;

    private final boolean showRequestLog;

    private final int maxRequestSize;

    @Autowired
    public HttpRequestParserImpl(List<FormDataParser> formDataParsers,
                                 LoggingService loggingService,
                                 JavacheConfigService configService) {
        this.formDataParsers = formDataParsers;
        this.loggingService = loggingService;
        this.showRequestLog = configService.getConfigParam(JavacheConfigValue.SHOW_REQUEST_LOG, boolean.class);
        this.maxRequestSize = configService.getConfigParam(JavacheConfigValue.MAX_REQUEST_SIZE, int.class);
    }

    /**
     * Reads the provided input stream and parses it using the HTTP protocol.
     *
     * @param inputStream - current request input stream.
     * @return populated {@link HttpRequest}
     */
    @Override
    public HttpRequest parseHttpRequest(InputStream inputStream,
                                        RequestHandlerSharedData sharedData) throws CannotParseRequestException {
        try {
            final HttpRequest request = new HttpRequestImpl();
            final Socket socket = sharedData.getObject(SharedDataPropertyNames.CLIENT_CONNECTION, Socket.class);
            request.setRemoteAddress(socket.getInetAddress().getHostAddress());

            final List<String> headers = parseMetadataLines(inputStream, false);

            this.setMethodAndURL(headers.get(0), request);
            this.addQueryParameters(headers.get(0), request);
            this.addHeaders(headers, request);
            this.initCookies(request);
            this.setContentLength(inputStream, request);
            if (request.getContentLength() > this.maxRequestSize) {
                throw new RequestTooBigException(REQUEST_TOO_BIG_MSG, request.getContentLength());
            }

            final String contentType = request.getContentType();
            final FormDataParserProvider formDataParserProvider = FormDataParserProvider.findByContentType(contentType);
            this.getParser(formDataParserProvider).parseBodyParams(inputStream, request);

            this.trimRequestPath(request);
            return request;
        } catch (IOException ex) {
            throw new CannotParseRequestException(ex.getMessage(), ex);
        }
    }

    private List<String> parseMetadataLines(InputStream inputStream, boolean allowNewLineWithoutReturn)
            throws CannotParseRequestException {
        try {
            final List<String> metadataLines = new ArrayList<>();

            StringBuilder metadataBuilder = new StringBuilder();
            boolean wasNewLine = true;
            int lineNumber = 1;
            int readBytesCount = 0;
            int b;

            while ((b = inputStream.read()) >= 0) {
                readBytesCount++;
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
                        throw new CannotParseRequestException(
                                String.format("Illegal character after return on line %d.", lineNumber)
                        );
                    }
                } else if (b == '\n') {
                    if (!allowNewLineWithoutReturn) {
                        throw new CannotParseRequestException(
                                String.format("Illegal new-line character without preceding return on line %d.", lineNumber)
                        );
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

            if (readBytesCount < 2) {
                throw new CannotParseRequestException("Request is empty");
            }

            if (this.showRequestLog) {
                this.loggingService.info(String.join("\n", metadataLines));
            }

            return metadataLines;
        } catch (IOException ex) {
            throw new CannotParseRequestException(ex.getMessage(), ex);
        }
    }

    private void setMethodAndURL(String requestFirstLine, HttpRequest request) {
        request.setMethod(requestFirstLine.split("\\s")[0]);
        request.setRequestURL(URLDecoder.decode(
                requestFirstLine.split("[\\s\\?]")[1],
                StandardCharsets.UTF_8
        ));
    }

    private void addHeaders(List<String> requestMetadata, HttpRequest request) {
        for (int i = 1; i < requestMetadata.size(); i++) {
            final String[] headerKeyValuePair = requestMetadata.get(i).split(":\\s+");
            request.addHeader(headerKeyValuePair[0], headerKeyValuePair[1]);
        }
    }

    private void addQueryParameters(String requestFirstLine, HttpRequest request) {
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

    private void initCookies(HttpRequest request) {
        if (request.getHeader(ToyoteConstants.COOKIE_HEADER_NAME) == null) {
            return;
        }

        final String[] allCookies = request.getHeader(ToyoteConstants.COOKIE_HEADER_NAME).split(";\\s");

        for (String cookieStr : allCookies) {
            final String[] cookieKeyValuePair = cookieStr.split("=");

            final String keyName = decode(cookieKeyValuePair[0]);
            final String value = cookieKeyValuePair.length > 1 ? decode(cookieKeyValuePair[1]) : null;

            request.getCookies().put(keyName, new HttpCookieImpl(keyName, value));
        }
    }

    private void setContentLength(InputStream inputStream, HttpRequest request) throws IOException {
        if (request.getHeader(ToyoteConstants.CONTENT_LENGTH) != null) {
            request.setContentLength(Integer.parseInt(request.getHeader(ToyoteConstants.CONTENT_LENGTH)));
        } else {
            request.setContentLength(inputStream.available());
        }
    }

    private static String decode(String str) {
        return URLDecoder.decode(str, StandardCharsets.UTF_8);
    }

    private FormDataParser getParser(FormDataParserProvider provider) {
        if (this.instanceProviderMap.containsKey(provider)) {
            return this.instanceProviderMap.get(provider);
        }

        final FormDataParser formDataParser = this.formDataParsers.stream()
                .filter(parser -> provider.getParserType().isAssignableFrom(parser.getClass()))
                .findFirst()
                .orElseThrow(() -> new CannotParseRequestException(String.format(
                        "Could not find %s form data parser", provider
                )));
        this.instanceProviderMap.put(provider, formDataParser);

        return formDataParser;
    }

    private void trimRequestPath(HttpRequest request) {
        request.setRequestURL(
                request.getRequestURL().replaceAll("\\.{2,}\\/?", "")
        );
    }
}
