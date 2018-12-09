package com.cyecize.javache.embedded.services;

import com.cyecize.broccolina.SoletDispatcher;
import com.cyecize.javache.api.RequestHandler;
import com.cyecize.javache.services.RequestHandlerLoadingService;
import com.cyecize.toyote.ResourceHandler;

import java.util.LinkedList;
import java.util.List;

public class EmbeddedRequestHandlerLoadingService implements RequestHandlerLoadingService {

    private final String workingDir;

    private LinkedList<RequestHandler> requestHandlers;

    public EmbeddedRequestHandlerLoadingService(String workingDir) {
        this.workingDir = workingDir;
        this.requestHandlers = new LinkedList<>();
    }

    @Override
    public void loadRequestHandlers(List<String> list) {
        this.requestHandlers.add(new SoletDispatcher(workingDir));
        this.requestHandlers.add(new ResourceHandler(workingDir));
    }

    @Override
    public List<RequestHandler> getRequestHandlers() {
        return this.requestHandlers;
    }
}
