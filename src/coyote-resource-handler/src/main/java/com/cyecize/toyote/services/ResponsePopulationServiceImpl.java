package com.cyecize.toyote.services;

import com.cyecize.http.HttpRequest;
import com.cyecize.http.HttpResponse;
import com.cyecize.http.HttpStatus;
import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.ioc.annotations.Service;

import java.io.File;
import java.io.IOException;

@Service
public class ResponsePopulationServiceImpl implements ResponsePopulationService {

    private final Tika tika;

    private final CacheControlService cacheControlService;

    @Autowired
    public ResponsePopulationServiceImpl(Tika tika, CacheControlService cacheControlService) {
        this.tika = tika;
        this.cacheControlService = cacheControlService;
    }

    @Override
    public void init() {
        this.cacheControlService.init();
    }

    /**
     * Populates {@link HttpResponse} with found resource.
     * Adds necessary headers that are required in order to transfer a resource using the HTTP protocol.
     */
    @Override
    public void handleResourceFoundResponse(HttpRequest request, HttpResponse response, File resourceFile, long fileSize)
            throws IOException {
        final String mediaType = this.tika.detect(resourceFile);

        response.setStatusCode(HttpStatus.OK);

        response.addHeader("Content-Type", mediaType);
        response.addHeader("Content-Length", fileSize + "");
        response.addHeader("Content-Disposition", "inline");
        this.cacheControlService.addCachingHeader(request, response, mediaType);
    }
}
