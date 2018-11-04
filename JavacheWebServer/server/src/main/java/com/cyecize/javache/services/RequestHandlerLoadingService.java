package com.cyecize.javache.services;

import com.cyecize.javache.api.RequestHandler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface RequestHandlerLoadingService {

    void loadRequestHandlers(List<String> requestHandlerPriority) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException;

    List<RequestHandler> getRequestHandlers();
}
