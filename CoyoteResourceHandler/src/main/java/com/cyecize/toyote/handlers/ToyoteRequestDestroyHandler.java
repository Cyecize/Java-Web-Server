package com.cyecize.toyote.handlers;

import com.cyecize.http.HttpRequest;
import com.cyecize.http.MultipartFile;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.javache.api.RequestDestroyHandler;
import com.cyecize.javache.api.RequestHandlerSharedData;
import com.cyecize.javache.api.SharedDataPropertyNames;

import java.io.IOException;

/**
 * Request handler called always after every request.
 * The purpose is to clear or dispose any left out resource to avoid memory leaks.
 */
@Service
public class ToyoteRequestDestroyHandler implements RequestDestroyHandler {

    @Override
    public void destroy(RequestHandlerSharedData sharedData) {
        final HttpRequest request = sharedData.getObject(SharedDataPropertyNames.HTTP_REQUEST, HttpRequest.class);
        if (request == null || request.getMultipartFiles() == null) {
            return;
        }

        for (MultipartFile multipartFile : request.getMultipartFiles()) {
            try {
                multipartFile.getInputStream().close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
