package com.cyecize.toyote;

import com.cyecize.http.HttpRequest;
import com.cyecize.http.HttpResponse;
import com.cyecize.http.HttpResponseImpl;
import com.cyecize.http.HttpStatus;
import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.javache.api.JavacheComponent;
import com.cyecize.javache.api.RequestHandler;
import com.cyecize.javache.api.RequestHandlerSharedData;
import com.cyecize.toyote.services.ErrorHandlingService;
import com.cyecize.toyote.services.HttpRequestParser;

import java.io.*;

@JavacheComponent
public class ToyoteRequestHandler implements RequestHandler {

    private final HttpRequestParser httpRequestParser;

    private final ErrorHandlingService errorHandlingService;

    @Autowired
    public ToyoteRequestHandler(HttpRequestParser httpRequestParser, ErrorHandlingService errorHandlingService) {
        this.httpRequestParser = httpRequestParser;
        this.errorHandlingService = errorHandlingService;
    }

    @Override
    public void init() {

    }

    @Override
    public boolean handleRequest(InputStream inputStream, OutputStream outputStream, RequestHandlerSharedData sharedData) throws IOException {

        try {
            final HttpRequest request = this.httpRequestParser.parseHttpRequest(inputStream);
            final HttpResponse response = new HttpResponseImpl();

            sharedData.addObject(ToyoteConstants.HTTP_REQUEST_SHARED_NAME, request);
            sharedData.addObject(ToyoteConstants.HTTP_RESPONSE_SHARED_NAME, response);
        } catch (Exception e) {
            return this.errorHandlingService.handleException(outputStream, e, new HttpResponseImpl(), HttpStatus.BAD_REQUEST);
        }

        return false;
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE;
    }
}
