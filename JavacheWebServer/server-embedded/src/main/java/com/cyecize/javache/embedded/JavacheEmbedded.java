package com.cyecize.javache.embedded;

import com.cyecize.ioc.MagicInjector;
import com.cyecize.ioc.config.MagicConfiguration;
import com.cyecize.ioc.services.DependencyContainer;
import com.cyecize.javache.api.IoC;
import com.cyecize.javache.core.Server;
import com.cyecize.javache.core.ServerImpl;
import com.cyecize.javache.embedded.internal.JavacheConfigBeanCreator;
import com.cyecize.javache.embedded.internal.JavacheEmbeddedComponent;
import com.cyecize.javache.services.LoggingService;
import com.cyecize.javache.services.RequestHandlerLoadingService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JavacheEmbedded {

    public static void startServer(int port, Class<?> mainClass) {
        startServer(port, new HashMap<>(), mainClass);
    }

    public static void startServer(int port, Map<String, Object> config, Class<?> mainClass, Runnable onServerLoadedEvent) {
        try {

            final MagicConfiguration magicConfiguration = new MagicConfiguration()
                    .scanning().addCustomServiceAnnotation(JavacheEmbeddedComponent.class)
                    .and()
                    .build();

            JavacheConfigBeanCreator.config = config;
            JavacheConfigBeanCreator.mainClass = mainClass;
            JavacheConfigBeanCreator.port = port;

            final DependencyContainer dependencyContainer = MagicInjector.run(JavacheEmbedded.class, magicConfiguration);
            IoC.setJavacheDependencyContainer(dependencyContainer);
            IoC.setRequestHandlersDependencyContainer(dependencyContainer);

            dependencyContainer.getService(RequestHandlerLoadingService.class).loadRequestHandlers(
                    new ArrayList<>(), null, null
            );

            final Server server = new ServerImpl(
                    port,
                    dependencyContainer.getService(LoggingService.class),
                    dependencyContainer.getService(RequestHandlerLoadingService.class)
            );

            if (onServerLoadedEvent != null) {
                onServerLoadedEvent.run();
            }

            server.run();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static void startServer(int port, Map<String, Object> config, Class<?> mainClass) {
        startServer(port, config, mainClass, null);
    }
}
