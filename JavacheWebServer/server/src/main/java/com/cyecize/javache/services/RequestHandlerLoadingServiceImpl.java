package com.cyecize.javache.services;

import com.cyecize.WebConstants;
import com.cyecize.ioc.MagicInjector;
import com.cyecize.ioc.config.MagicConfiguration;
import com.cyecize.ioc.services.DependencyContainer;
import com.cyecize.javache.api.IoC;
import com.cyecize.javache.api.JavacheComponent;
import com.cyecize.javache.api.RequestHandler;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@JavacheComponent
public class RequestHandlerLoadingServiceImpl implements RequestHandlerLoadingService {

    private final List<RequestHandler> requestHandlers;

    public RequestHandlerLoadingServiceImpl() {
        this.requestHandlers = new LinkedList<>();
    }

    @Override
    public void loadRequestHandlers(List<String> requestHandlerPriority, List<File> libJarFiles) {

        final MagicConfiguration magicConfiguration = new MagicConfiguration()
                .scanning()
                .addCustomServiceAnnotations(WebConstants.JAVACHE_IOC_CONFIGURATION.scanning().getCustomServiceAnnotations())
                .and()
                .instantiations()
                .addProvidedServices(IoC.getJavacheDependencyContainer().getAllServices())
                .and()
                .build();

        final DependencyContainer javacheComponentsContainer = MagicInjector.run(
                libJarFiles.stream()
                        .filter(jarFile -> requestHandlerPriority.stream().anyMatch(rh -> jarFile.getName().endsWith(rh + ".jar")))
                        .toArray(File[]::new),
                magicConfiguration
        );

        this.requestHandlers.addAll(
                javacheComponentsContainer.getImplementations(RequestHandler.class)
                        .stream()
                        .map(sd -> (RequestHandler) sd.getProxyInstance())
                        .sorted(Comparator.comparingInt(RequestHandler::order))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public List<RequestHandler> getRequestHandlers() {
        return Collections.unmodifiableList(this.requestHandlers);
    }
}
