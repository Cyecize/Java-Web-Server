package com.cyecize.javache.core;

import com.cyecize.WebConstants;
import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.javache.ConfigConstants;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.javache.services.LoggingService;
import com.cyecize.javache.services.RequestHandlerLoadingServiceImpl;

import java.io.IOException;

@Service
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
        final String[] startupArgs = this.configService.getConfigParam(ConfigConstants.SERVER_STARTUP_ARGS, String[].class);

        int port = WebConstants.DEFAULT_SERVER_PORT;
        if (startupArgs.length > 0) {
            port = Integer.parseInt(startupArgs[0]);
        }

        this.configService.addConfigParam(ConfigConstants.SERVER_PORT, port);
        this.configService.addConfigParam(ConfigConstants.SERVER_STARTUP_ARGS, startupArgs);

        final Server server = new ServerImpl(port, loggingService, new RequestHandlerLoadingServiceImpl(configService), configService);

        try {
            server.run();
        } catch (IOException ex) {
            loggingService.error(ex.getMessage());
            loggingService.printStackTrace(ex.getStackTrace());
        }
    }
}
