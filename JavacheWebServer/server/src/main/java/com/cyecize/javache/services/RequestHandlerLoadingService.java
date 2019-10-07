package com.cyecize.javache.services;

import com.cyecize.javache.api.RequestHandler;

import java.io.File;
import java.util.List;

public interface RequestHandlerLoadingService {

    void loadRequestHandlers(List<String> requestHandlerPriority, List<File> libJarFiles);

    List<RequestHandler> getRequestHandlers();
}
