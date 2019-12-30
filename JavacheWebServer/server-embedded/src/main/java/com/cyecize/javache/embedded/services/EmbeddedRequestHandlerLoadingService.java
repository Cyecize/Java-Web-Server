package com.cyecize.javache.embedded.services;

import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.javache.api.IoC;
import com.cyecize.javache.api.RequestDestroyHandler;
import com.cyecize.javache.api.RequestHandler;
import com.cyecize.javache.embedded.internal.JavacheEmbeddedComponent;
import com.cyecize.javache.services.RequestHandlerLoadingService;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@JavacheEmbeddedComponent
public class EmbeddedRequestHandlerLoadingService implements RequestHandlerLoadingService {

    private final LinkedList<RequestHandler> requestHandlers;

    private final List<RequestDestroyHandler> destroyHandlers;

    @Autowired
    public EmbeddedRequestHandlerLoadingService() {
        this.requestHandlers = new LinkedList<>();
        this.destroyHandlers = new ArrayList<>();
    }

    @Override
    public void loadRequestHandlers(List<String> requestHandlerPriority, Map<File, URL> libURLs, Map<File, URL> apiURLs) {
        this.requestHandlers.addAll(IoC.getJavacheDependencyContainer().getImplementations(RequestHandler.class)
                .stream()
                .map(sd -> (RequestHandler) sd.getInstance())
                .sorted(Comparator.comparingInt(RequestHandler::order))
                .peek(RequestHandler::init)
                .collect(Collectors.toList()));

        this.destroyHandlers.addAll(IoC.getJavacheDependencyContainer().getImplementations(RequestDestroyHandler.class)
                .stream()
                .map(sd -> (RequestDestroyHandler) sd.getInstance())
                .collect(Collectors.toList())
        );
    }

    @Override
    public List<RequestHandler> getRequestHandlers() {
        return this.requestHandlers;
    }

    @Override
    public List<RequestDestroyHandler> getRequestDestroyHandlers() {
        return this.destroyHandlers;
    }

}
