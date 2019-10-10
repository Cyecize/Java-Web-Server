package com.cyecize.toyote;

import com.cyecize.http.HttpRequest;
import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.javache.api.JavacheComponent;
import com.cyecize.javache.api.RequestHandler;
import com.cyecize.javache.api.RequestHandlerSharedData;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.toyote.services.HttpRequestParser;

import java.io.*;

@JavacheComponent
public class ResourceHandler implements RequestHandler {

    private static final String HTTP_REQUEST_SHARED_NAME = "HTTP_REQUEST";

    //TODO make use of this
    private final JavacheConfigService configService;

    private final HttpRequestParser httpRequestParser;

    private boolean hasIntercepted;

    @Autowired
    public ResourceHandler(JavacheConfigService configService, HttpRequestParser httpRequestParser) {
        this.configService = configService;
        this.httpRequestParser = httpRequestParser;
        this.hasIntercepted = false;
    }

    @Override
    public void init() {
        System.out.println("Loaded Toyote");
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, RequestHandlerSharedData sharedData) throws IOException {
        try {
            final HttpRequest request = this.httpRequestParser.parseHttpRequest(inputStream);

            sharedData.addObject(HTTP_REQUEST_SHARED_NAME, request);
        } catch (IOException e) {
            this.hasIntercepted = true;
            throw new IOException(e);
        }
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
