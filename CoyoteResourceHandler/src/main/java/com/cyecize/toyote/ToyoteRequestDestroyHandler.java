package com.cyecize.toyote;

import com.cyecize.http.HttpRequest;
import com.cyecize.http.MultipartFile;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.javache.api.RequestDestroyHandler;
import com.cyecize.javache.api.RequestHandlerSharedData;

import java.io.IOException;

@Service
public class ToyoteRequestDestroyHandler implements RequestDestroyHandler {

    @Override
    public void destroy(RequestHandlerSharedData sharedData) {
        final HttpRequest request = (HttpRequest) sharedData.getObject(ToyoteConstants.HTTP_REQUEST_SHARED_NAME);

        for (MultipartFile multipartFile : request.getMultipartFiles()) {
            try {
                multipartFile.getInputStream().close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
