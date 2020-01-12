package com.cyecize.javache.core;

import com.cyecize.WebConstants;
import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.javache.services.LibraryLoadingService;
import com.cyecize.javache.services.LoggingService;
import com.cyecize.javache.services.RequestHandlerLoadingService;

import java.io.IOException;

@Service
public class ServerInitializerImpl implements ServerInitializer {

    private final LoggingService loggingService;

    private final JavacheConfigService configService;

    private final LibraryLoadingService libraryLoadingService;

    private final RequestHandlerLoadingService requestHandlerLoadingService;

    @Autowired
    public ServerInitializerImpl(LoggingService loggingService, JavacheConfigService configService,
                                 LibraryLoadingService libraryLoadingService,
                                 RequestHandlerLoadingService requestHandlerLoadingService) {
        this.loggingService = loggingService;
        this.configService = configService;
        this.libraryLoadingService = libraryLoadingService;
        this.requestHandlerLoadingService = requestHandlerLoadingService;
    }

    @Override
    public void initializeServer() {
        this.libraryLoadingService.loadLibraries();
        this.requestHandlerLoadingService.loadRequestHandlers(
                this.configService.getRequestHandlerPriority(),
                this.libraryLoadingService.getLibURLs(),
                this.libraryLoadingService.getApiURLs()
        );

        final String[] startupArgs = this.configService.getConfigParam(JavacheConfigValue.SERVER_STARTUP_ARGS, String[].class);

        if (this.configService.getConfigParam(JavacheConfigValue.SERVER_PORT, int.class) == WebConstants.JAVACHE_CONFIG_EMPTY_PORT) {
            int port = WebConstants.DEFAULT_SERVER_PORT;
            if (startupArgs.length > 0) {
                port = Integer.parseInt(startupArgs[0]);
            }

            this.configService.addConfigParam(JavacheConfigValue.SERVER_PORT, port);
        }

        final Server server = new ServerImpl(
                this.configService.getConfigParam(JavacheConfigValue.SERVER_PORT, int.class),
                this.loggingService,
                this.requestHandlerLoadingService
        );

        try {
            server.run();
        } catch (IOException ex) {
            this.loggingService.error(ex.getMessage());
            this.loggingService.printStackTrace(ex.getStackTrace());
        }
    }
}
