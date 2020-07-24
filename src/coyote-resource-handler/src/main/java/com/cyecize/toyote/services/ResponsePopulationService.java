package com.cyecize.toyote.services;

import com.cyecize.http.HttpRequest;
import com.cyecize.http.HttpResponse;

import java.io.File;
import java.io.IOException;

public interface ResponsePopulationService {

    void init();

    void handleResourceFoundResponse(HttpRequest request, HttpResponse response, File resourceFile, long fileSize) throws IOException;
}
