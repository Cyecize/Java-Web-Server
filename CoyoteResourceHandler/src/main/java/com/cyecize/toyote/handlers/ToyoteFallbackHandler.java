package com.cyecize.toyote.handlers;

import com.cyecize.http.HttpResponse;
import com.cyecize.http.HttpStatus;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.javache.api.RequestHandler;
import com.cyecize.javache.api.RequestHandlerSharedData;
import com.cyecize.javache.api.SharedDataPropertyNames;
import com.cyecize.toyote.ToyoteConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Service
public class ToyoteFallbackHandler implements RequestHandler {

    @Override
    public void init() {

    }

    @Override
    public boolean handleRequest(InputStream inputStream, OutputStream outputStream, RequestHandlerSharedData sharedData)
            throws IOException {
        final HttpResponse response = sharedData.getObject(SharedDataPropertyNames.HTTP_RESPONSE, HttpResponse.class);

        response.setStatusCode(HttpStatus.NOT_FOUND);
        response.setContent("The resource you are looking for could not be found!");

        outputStream.write(response.getBytes());

        return true;
    }

    @Override
    public int order() {
        return Integer.MAX_VALUE;
    }
}
