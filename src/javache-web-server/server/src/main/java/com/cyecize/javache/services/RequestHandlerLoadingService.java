package com.cyecize.javache.services;

import com.cyecize.javache.api.RequestDestroyHandler;
import com.cyecize.javache.api.RequestHandler;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

public interface RequestHandlerLoadingService {

    void loadRequestHandlers(List<String> requestHandlerFileNames, Map<File, URL> libURLs, Map<File, URL> apiURLs);

    List<RequestHandler> getRequestHandlers();

    List<RequestDestroyHandler> getRequestDestroyHandlers();
}
