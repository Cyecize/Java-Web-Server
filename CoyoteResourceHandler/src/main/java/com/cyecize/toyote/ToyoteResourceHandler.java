package com.cyecize.toyote;

import com.cyecize.http.HttpRequest;
import com.cyecize.http.HttpResponse;
import com.cyecize.http.HttpStatus;
import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.javache.api.RequestHandler;
import com.cyecize.javache.api.RequestHandlerSharedData;
import com.cyecize.toyote.exceptions.ResourceNotFoundException;
import com.cyecize.toyote.services.ResourceLocationService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Service
public class ToyoteResourceHandler implements RequestHandler {

    private final ResourceLocationService resourceLocationService;

    private boolean hasIntercepted;

    @Autowired
    public ToyoteResourceHandler(ResourceLocationService resourceLocationService) {
        this.resourceLocationService = resourceLocationService;
        this.hasIntercepted = false;
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, RequestHandlerSharedData requestHandlerSharedData) throws IOException {
        this.hasIntercepted = false;

        final HttpRequest request = (HttpRequest) requestHandlerSharedData.getObject(ToyoteConstants.HTTP_REQUEST_SHARED_NAME);
        final HttpResponse response = (HttpResponse) requestHandlerSharedData.getObject(ToyoteConstants.HTTP_RESPONSE_SHARED_NAME);

        try (InputStream fileInputStream = this.resourceLocationService.locateResource(request.getRequestURL())) {
            this.createResponse(fileInputStream, response);

            outputStream.write(response.getBytes());

            final byte[] buffer = new byte[2048];
            int read;
            while ((read = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            this.hasIntercepted = true;
        } catch (ResourceNotFoundException ignored) { }
    }

    private void createResponse(InputStream inputStream, HttpResponse response) throws IOException {
        response.setStatusCode(HttpStatus.OK);

        response.addHeader("Content-Type", "img/png");//TODO probe this
        response.addHeader("Content-Length", inputStream.available() + "");
        response.addHeader("Content-Disposition", "inline");
        //TODO cache
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
}
