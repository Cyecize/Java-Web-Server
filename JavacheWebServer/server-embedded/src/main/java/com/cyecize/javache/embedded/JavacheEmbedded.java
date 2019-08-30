package com.cyecize.javache.embedded;

import com.cyecize.StartUp;
import com.cyecize.broccolina.services.ApplicationScanningService;
import com.cyecize.broccolina.services.JarFileUnzipService;
import com.cyecize.broccolina.services.JarFileUnzipServiceImpl;
import com.cyecize.javache.ConfigConstants;
import com.cyecize.javache.core.Server;
import com.cyecize.javache.core.ServerImpl;
import com.cyecize.javache.embedded.services.EmbeddedApplicationScanningService;
import com.cyecize.javache.embedded.services.EmbeddedJavacheConfigService;
import com.cyecize.javache.embedded.services.EmbeddedRequestHandlerLoadingService;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.javache.services.LoggingService;
import com.cyecize.javache.services.LoggingServiceImpl;
import com.cyecize.javache.services.RequestHandlerLoadingService;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class JavacheEmbedded {

    public static void startServer(int port, Class<?> mainClass) {
        startServer(port, new HashMap<>(), mainClass);
    }

    public static RequestHandlerLoadingService requestHandlerLoadingService;

    /**
     * Replaces system classloader with an instance of URLClassLoader
     * Extracts working directory from the given startup class.
     * Sets runtime config for Embedded server. NOTE that this config is tested on intelliJ.
     * If a problem occurs, you can pass your own properties for file location.
     * <p>
     * The working directory will be printed as the program starts. Check and see if
     * you compile output matches the working directory.
     * If if doesn't consider changing the properties.
     * <p>
     * Creates server instance and runs the server.
     * <p>
     * Calls event when application is loaded
     */
    public static void startServer(int port, Map<String, Object> config, Class<?> mainClass, Runnable onServerLoadedEvent) {
        try {
            StartUp.replaceSystemClassLoader();

            String workingDir = mainClass.getProtectionDomain().getCodeSource().getLocation().getFile().substring(1);

            if (workingDir.endsWith(".jar")) {
                JarFileUnzipService unzipService = new JarFileUnzipServiceImpl();
                unzipService.unzipJar(new File(workingDir), false, workingDir.replace(".jar", ""));
                workingDir = workingDir.replace(".jar", "");
            }

            System.out.println(String.format("Working Directory: %s", workingDir));

            final LoggingService loggingService = new LoggingServiceImpl();

            //Since classes is the default output directory.
            config.putIfAbsent(ConfigConstants.MAIN_APP_JAR_NAME, "classes");

            //There is not "classes" folder inside the jar file so we set it to empty.
            config.put(ConfigConstants.APP_COMPILE_OUTPUT_DIR_NAME, "");

            //Because of how Broccolina and Toyote read their request handlers, we want to go one step back.
            config.put(ConfigConstants.WEB_APPS_DIR_NAME, "../");

            JavacheConfigService configService = new EmbeddedJavacheConfigService(config);
            configService.addConfigParam(ConfigConstants.SERVER_PORT, port);

            ApplicationScanningService scanningService = new EmbeddedApplicationScanningService(configService, workingDir);

            requestHandlerLoadingService = new EmbeddedRequestHandlerLoadingService(workingDir, configService, scanningService);
            Server server = new ServerImpl(port, loggingService, requestHandlerLoadingService, configService);

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
