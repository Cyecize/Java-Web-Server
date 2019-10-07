package com.cyecize.javache.core;

import com.cyecize.WebConstants;
import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.api.JavacheComponent;
import com.cyecize.javache.services.*;

import java.io.IOException;

@JavacheComponent
public class ServerInitializerImpl implements ServerInitializer {

    private final LoggingService loggingService;

    private final JavacheConfigService configService;

    private final LibraryLoadingService libraryLoadingService;

    private final RequestHandlerLoadingService requestHandlerLoadingService;

    @Autowired
    public ServerInitializerImpl(LoggingService loggingService, JavacheConfigService configService, LibraryLoadingService libraryLoadingService, RequestHandlerLoadingService requestHandlerLoadingService) {
        this.loggingService = loggingService;
        this.configService = configService;
        this.libraryLoadingService = libraryLoadingService;
        this.requestHandlerLoadingService = requestHandlerLoadingService;
    }

    @Override
    public void initializeServer() {
        this.libraryLoadingService.loadLibraries();
        this.requestHandlerLoadingService.loadRequestHandlers(this.configService.getRequestHandlerPriority(), this.libraryLoadingService.getJarLibs());

        final String[] startupArgs = this.configService.getConfigParam(JavacheConfigValue.SERVER_STARTUP_ARGS, String[].class);

        int port = WebConstants.DEFAULT_SERVER_PORT;
        if (startupArgs.length > 0) {
            port = Integer.parseInt(startupArgs[0]);
        }

        this.configService.addConfigParam(JavacheConfigValue.SERVER_PORT, port);
        this.configService.addConfigParam(JavacheConfigValue.SERVER_STARTUP_ARGS, startupArgs);

        final Server server = new ServerImpl(port, this.loggingService, this.requestHandlerLoadingService, this.configService);

        try {
            server.run();
        } catch (IOException ex) {
            loggingService.error(ex.getMessage());
            loggingService.printStackTrace(ex.getStackTrace());
        }
    }
}
