package com.cyecize.toyote;

import com.cyecize.http.HttpRequest;
import com.cyecize.http.HttpResponse;
import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.javache.api.RequestHandler;
import com.cyecize.javache.api.RequestHandlerSharedData;

import com.cyecize.toyote.exceptions.ResourceNotFoundException;
import com.cyecize.toyote.services.ResourceLocationService;
import com.cyecize.toyote.services.ResponsePopulationService;

import java.io.*;

@Service
public class ToyoteResourceHandler implements RequestHandler {

    private final ResourceLocationService resourceLocationService;

    private final ResponsePopulationService responsePopulationService;

    private boolean hasIntercepted;

    @Autowired
    public ToyoteResourceHandler(ResourceLocationService resourceLocationService, ResponsePopulationService responsePopulationService) {
        this.resourceLocationService = resourceLocationService;
        this.responsePopulationService = responsePopulationService;
        this.hasIntercepted = false;
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, RequestHandlerSharedData requestHandlerSharedData) throws IOException {
        this.hasIntercepted = false;

        final HttpRequest request = (HttpRequest) requestHandlerSharedData.getObject(ToyoteConstants.HTTP_REQUEST_SHARED_NAME);
        final HttpResponse response = (HttpResponse) requestHandlerSharedData.getObject(ToyoteConstants.HTTP_RESPONSE_SHARED_NAME);

        try {
            final File resource = this.resourceLocationService.locateResource(request.getRequestURL());

            try (final FileInputStream fileInputStream = new FileInputStream(resource)) {
                this.responsePopulationService.handleResourceFoundResponse(request, response, resource, fileInputStream.available());

                outputStream.write(response.getBytes());
                this.transferStream(fileInputStream, outputStream);
            }

            this.hasIntercepted = true;
        } catch (ResourceNotFoundException ignored) {
        }
    }

    @Override
    public void init() {

    }

    @Override
    public boolean hasIntercepted() {
        return this.hasIntercepted;
    }

    @Override
    public int order() {
        return 2;
    }

    private void transferStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        final byte[] buffer = new byte[2048];
        int read;

        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
    }
}
