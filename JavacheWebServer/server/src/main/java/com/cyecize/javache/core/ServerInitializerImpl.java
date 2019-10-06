package com.cyecize.javache.core;

import com.cyecize.WebConstants;
import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.api.JavacheComponent;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.javache.services.LoggingService;
import com.cyecize.javache.services.RequestHandlerLoadingServiceImpl;

import java.io.IOException;

@JavacheComponent
public class ServerInitializerImpl implements ServerInitializer {

    private final LoggingService loggingService;

    private final JavacheConfigService configService;

    @Autowired
    public ServerInitializerImpl(LoggingService loggingService, JavacheConfigService configService) {
        this.loggingService = loggingService;
        this.configService = configService;
    }

    @Override
    public void initializeServer() {
        final String[] startupArgs = this.configService.getConfigParam(JavacheConfigValue.SERVER_STARTUP_ARGS, String[].class);

        int port = WebConstants.DEFAULT_SERVER_PORT;
        if (startupArgs.length > 0) {
            port = Integer.parseInt(startupArgs[0]);
        }

        this.configService.addConfigParam(JavacheConfigValue.SERVER_PORT, port);
        this.configService.addConfigParam(JavacheConfigValue.SERVER_STARTUP_ARGS, startupArgs);

        final Server server = new ServerImpl(port, loggingService, new RequestHandlerLoadingServiceImpl(configService), configService);

        try {
            server.run();
        } catch (IOException ex) {
            loggingService.error(ex.getMessage());
            loggingService.printStackTrace(ex.getStackTrace());
        }
    }
}
