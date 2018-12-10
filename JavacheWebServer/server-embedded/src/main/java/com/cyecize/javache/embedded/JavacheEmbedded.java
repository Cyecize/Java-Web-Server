package com.cyecize.javache.embedded;

import com.cyecize.StartUp;
import com.cyecize.WebConstants;
import com.cyecize.javache.ConfigConstants;
import com.cyecize.javache.core.Server;
import com.cyecize.javache.core.ServerImpl;
import com.cyecize.javache.embedded.services.EmbeddedJavacheConfigService;
import com.cyecize.javache.embedded.services.EmbeddedRequestHandlerLoadingService;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.javache.services.LoggingService;
import com.cyecize.javache.services.LoggingServiceImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JavacheEmbedded {

    public static void startServer(int port, Class<?> mainClass) {
        startServer(port, new HashMap<>(), mainClass);
    }

    public static void startServer(int port, Map<String, Object> config,  Class<?> mainClass) {
        try {
            StartUp.replaceSystemClassLoader();

            String workingDir = mainClass.getProtectionDomain().getCodeSource().getLocation().getFile().substring(1);
            System.out.println(String.format("Working Directory: %s", workingDir));

            final LoggingService loggingService = new LoggingServiceImpl();

            config.put(ConfigConstants.MAIN_APP_JAR_NAME, "classes");
            config.put(ConfigConstants.APP_COMPILE_OUTPUT_DIR_NAME, "");
            config.put(ConfigConstants.WEB_APPS_DIR_NAME, "../");

            JavacheConfigService configService = new EmbeddedJavacheConfigService(config);

            Server server = new ServerImpl(port, loggingService, new EmbeddedRequestHandlerLoadingService(workingDir, configService), configService);

            try {
                server.run();
            } catch (IOException ex) {
                loggingService.error(ex.getMessage());
                loggingService.printStackTrace(ex.getStackTrace());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
