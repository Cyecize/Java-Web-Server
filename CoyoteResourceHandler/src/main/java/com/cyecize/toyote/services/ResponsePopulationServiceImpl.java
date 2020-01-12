package com.cyecize.toyote.services;

import com.cyecize.http.HttpRequest;
import com.cyecize.http.HttpResponse;
import com.cyecize.http.HttpStatus;
import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.javache.services.JavacheConfigService;

import java.io.File;
import java.io.IOException;

@Service
public class ResponsePopulationServiceImpl implements ResponsePopulationService {

    private final Tika tika;

    @Autowired
    public ResponsePopulationServiceImpl(JavacheConfigService configService, Tika tika) {
        this.tika = tika;
    }

    @Override
    public void handleResourceFoundResponse(HttpRequest request, HttpResponse response, File resourceFile, long fileSize)
            throws IOException {
        response.setStatusCode(HttpStatus.OK);

        response.addHeader("Content-Type", this.tika.detect(resourceFile));
        response.addHeader("Content-Length", fileSize + "");
        response.addHeader("Content-Disposition", "inline");
        //TODO cache
    }
}
