package com.cyecize.toyote.handlers;

import com.cyecize.http.HttpRequest;
import com.cyecize.http.MultipartFile;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.javache.api.RequestDestroyHandler;
import com.cyecize.javache.api.RequestHandlerSharedData;
import com.cyecize.toyote.ToyoteConstants;

import java.io.IOException;

@Service
public class ToyoteRequestDestroyHandler implements RequestDestroyHandler {

    @Override
    public void destroy(RequestHandlerSharedData sharedData) {
        final HttpRequest request = (HttpRequest) sharedData.getObject(ToyoteConstants.HTTP_REQUEST_SHARED_NAME);
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
