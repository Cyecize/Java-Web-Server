package com.cyecize.toyote.handlers;

import com.cyecize.http.HttpRequest;
import com.cyecize.http.HttpResponse;
import com.cyecize.http.HttpResponseImpl;
import com.cyecize.http.HttpStatus;
import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.javache.api.RequestHandler;
import com.cyecize.javache.api.RequestHandlerSharedData;
import com.cyecize.javache.api.SharedDataPropertyNames;
import com.cyecize.toyote.exceptions.RequestTooBigException;
import com.cyecize.toyote.services.ErrorHandlingService;
import com.cyecize.toyote.services.HttpRequestParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Request handler responsible for reading and parsing the request input stream.
 * This request is always first to be executed.
 */
@Service
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
    public boolean handleRequest(InputStream inputStream, OutputStream outputStream, RequestHandlerSharedData sharedData)
            throws IOException {
        try {
            final HttpRequest request = this.httpRequestParser.parseHttpRequest(inputStream, sharedData);
            final HttpResponse response = new HttpResponseImpl();

            sharedData.addObject(SharedDataPropertyNames.HTTP_REQUEST, request);
            sharedData.addObject(SharedDataPropertyNames.HTTP_RESPONSE, response);
        } catch (RequestTooBigException ex) {
            this.disposeInputStream(ex.getContentLength(), inputStream);
            return this.errorHandlingService.handleRequestTooBig(outputStream, ex, new HttpResponseImpl());
        } catch (Exception e) {
            return this.errorHandlingService.handleException(outputStream, e, new HttpResponseImpl(), HttpStatus.BAD_REQUEST);
        }

        return false;
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE;
    }

    /**
     * The purpose of this method is to read the input stream before closing it
     * otherwise the TCP connection will not be closed properly.
     */
    private void disposeInputStream(int length, InputStream inputStream) throws IOException {
        byte[] buffer = new byte[0];
        int leftToRead = length;
        int bytesRead = Math.min(2048, inputStream.available());

        while (leftToRead > 0) {
            buffer = inputStream.readNBytes(bytesRead);
            leftToRead -= bytesRead;
            bytesRead = Math.min(2048, inputStream.available());
        }

        buffer = null;
    }
}
