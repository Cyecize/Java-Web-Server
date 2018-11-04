package com.cyecize;

import com.cyecize.javache.core.Server;
import com.cyecize.javache.core.ServerImpl;
import com.cyecize.javache.services.JavacheConfigServiceImpl;
import com.cyecize.javache.services.LoggingService;
import com.cyecize.javache.services.LoggingServiceImpl;
import com.cyecize.javache.services.RequestHandlerLoadingServiceImpl;

import java.io.IOException;

public class StartUp {

    public static void main(String[] args) {
        final LoggingService loggingService = new LoggingServiceImpl();
        int port = WebConstants.DEFAULT_SERVER_PORT;

        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }

        Server server = new ServerImpl(port, loggingService, new RequestHandlerLoadingServiceImpl(), new JavacheConfigServiceImpl());

        try {
            server.run();
        } catch (IOException ex) {
            loggingService.error(ex.getMessage());
            loggingService.printStackTrace(ex.getStackTrace());
        }
    }
}
