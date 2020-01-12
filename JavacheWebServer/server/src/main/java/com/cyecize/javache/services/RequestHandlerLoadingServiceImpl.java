package com.cyecize.javache.services;

import com.cyecize.WebConstants;
import com.cyecize.ioc.MagicInjector;
import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.ioc.config.MagicConfiguration;
import com.cyecize.ioc.services.DependencyContainer;
import com.cyecize.javache.api.IoC;
import com.cyecize.javache.api.JavacheComponent;
import com.cyecize.javache.api.RequestDestroyHandler;
import com.cyecize.javache.api.RequestHandler;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JavacheComponent
public class RequestHandlerLoadingServiceImpl implements RequestHandlerLoadingService {

    private final List<RequestHandler> requestHandlers;

    private final List<RequestDestroyHandler> destroyHandlers;

    @Autowired
    public RequestHandlerLoadingServiceImpl() {
        this.requestHandlers = new LinkedList<>();
        this.destroyHandlers = new ArrayList<>();
    }

    @Override
    public void loadRequestHandlers(List<String> requestHandlerPriority, Map<File, URL> libURLs, Map<File, URL> apiURLs) {

        IoC.setApiClassLoader(new URLClassLoader(apiURLs.values().toArray(URL[]::new)));
        IoC.setRequestHandlersClassLoader(new URLClassLoader(libURLs.values().toArray(URL[]::new), IoC.getApiClassLoader()));

        final MagicConfiguration magicConfiguration = new MagicConfiguration()
                .scanning()
                .addCustomServiceAnnotations(WebConstants.JAVACHE_IOC_CONFIGURATION.scanning().getCustomServiceAnnotations())
                .setClassLoader(IoC.getRequestHandlersClassLoader())
                .and()
                .instantiations()
                .addProvidedServices(IoC.getJavacheDependencyContainer().getAllServices())
                .and()
                .build();

        final DependencyContainer requestHandlersDependencyContainer = MagicInjector.run(
                libURLs.keySet().stream()
                        .filter(jarFile -> requestHandlerPriority.stream().anyMatch(rh -> jarFile.getName().endsWith(rh + ".jar")))
                        .toArray(File[]::new),
                magicConfiguration
        );

        IoC.setRequestHandlersDependencyContainer(requestHandlersDependencyContainer);

        this.requestHandlers.addAll(
                requestHandlersDependencyContainer.getImplementations(RequestHandler.class)
                        .stream()
                        .map(sd -> (RequestHandler) sd.getInstance())
                        .sorted(Comparator.comparingInt(RequestHandler::order))
                        .collect(Collectors.toList())
        );

        this.destroyHandlers.addAll(
                requestHandlersDependencyContainer.getImplementations(RequestDestroyHandler.class)
                        .stream()
                        .map(sd -> (RequestDestroyHandler) sd.getInstance())
                        .collect(Collectors.toList())
        );

        this.requestHandlers.forEach(RequestHandler::init);
    }

    @Override
    public List<RequestHandler> getRequestHandlers() {
        return Collections.unmodifiableList(this.requestHandlers);
    }

    @Override
    public List<RequestDestroyHandler> getRequestDestroyHandlers() {
        return this.destroyHandlers;
    }
}
