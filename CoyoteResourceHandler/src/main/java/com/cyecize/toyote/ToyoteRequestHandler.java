package com.cyecize.toyote;

import com.cyecize.http.HttpRequest;
import com.cyecize.http.HttpResponse;
import com.cyecize.http.HttpResponseImpl;
import com.cyecize.http.HttpStatus;
import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.javache.api.JavacheComponent;
import com.cyecize.javache.api.RequestHandler;
import com.cyecize.javache.api.RequestHandlerSharedData;
import com.cyecize.toyote.services.HttpRequestParser;

import java.io.*;

@JavacheComponent
public class ToyoteRequestHandler implements RequestHandler {
    private final HttpRequestParser httpRequestParser;

    private boolean hasIntercepted;

    @Autowired
    public ToyoteRequestHandler(HttpRequestParser httpRequestParser) {
        this.httpRequestParser = httpRequestParser;
        this.hasIntercepted = false;
    }

    @Override
    public void init() {

    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, RequestHandlerSharedData sharedData) throws IOException {
        this.hasIntercepted = false;

        try {
            final HttpRequest request = this.httpRequestParser.parseHttpRequest(inputStream);
            final HttpResponse response = new HttpResponseImpl();

            sharedData.addObject(ToyoteConstants.HTTP_REQUEST_SHARED_NAME, request);
            sharedData.addObject(ToyoteConstants.HTTP_RESPONSE_SHARED_NAME, response);
        } catch (Exception e) {
            this.hasIntercepted = true;
            this.writeErrorResponse(outputStream, e);
        }
    }

    //TODO: create service for handling errors, add config for showing stack trace.
    //TODO: put 404 handler in the same error handler service for Resource handling.
    private void writeErrorResponse(OutputStream outputStream, Exception ex) throws IOException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        ex.printStackTrace(new PrintStream(os));

        final HttpResponse response = new HttpResponseImpl();
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        response.setContent(os.toByteArray());

        outputStream.write(response.getBytes());
    }

    @Override
    public boolean hasIntercepted() {
        return this.hasIntercepted;
    }

    @Override
    public int order() {
        return 0;
    }
}
