package com.cyecize;

import com.cyecize.javache.core.Server;
import com.cyecize.javache.core.ServerImpl;
import com.cyecize.javache.services.JavacheConfigServiceImpl;
import com.cyecize.javache.services.LoggingService;
import com.cyecize.javache.services.LoggingServiceImpl;
import com.cyecize.javache.services.RequestHandlerLoadingServiceImpl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.stream.Collectors;

public class StartUp {

    public static void main(String[] args) throws Exception {
        replaceSystemClassLoader();
        final LoggingService loggingService = new LoggingServiceImpl();
        int port = WebConstants.DEFAULT_SERVER_PORT;

        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        Server server = new ServerImpl(port, loggingService, new RequestHandlerLoadingServiceImpl(), new JavacheConfigServiceImpl());

        try {
            server.run();
        } catch (IOException ex) {
            loggingService.error(ex.getMessage());
            loggingService.printStackTrace(ex.getStackTrace());
        }
    }

    /*
        This is a workaround for java 9 and above.
        Since java 9 the systemClassLoader is no longer URLClassLoader
        which means that there is no method "addUrl"
        by doing this we change the default classLoader with URLClassLoader
     */
    private static void replaceSystemClassLoader() throws IllegalAccessException {
        Field scl = Arrays.stream(ClassLoader.class.getDeclaredFields())
                .filter(f -> f.getType() == ClassLoader.class && !f.getName().equals("parent"))
                .findFirst().orElse(null);

        scl.setAccessible(true);
        scl.set(null, new URLClassLoader(new URL[0]));
        Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
    }
}
