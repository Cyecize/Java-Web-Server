package com.cyecize.javache.embedded.services;

import com.cyecize.broccolina.SoletDispatcher;
import com.cyecize.broccolina.services.ApplicationLoadingServiceImpl;
import com.cyecize.broccolina.services.ApplicationScanningService;
import com.cyecize.javache.ConfigConstants;
import com.cyecize.javache.api.RequestHandler;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.javache.services.RequestHandlerLoadingService;
import com.cyecize.toyote.ResourceHandler;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class EmbeddedRequestHandlerLoadingService implements RequestHandlerLoadingService {

    private final String workingDir;

    private final JavacheConfigService configService;

    private final ApplicationScanningService scanningService;

    private LinkedList<RequestHandler> requestHandlers;

    public EmbeddedRequestHandlerLoadingService(String workingDir, JavacheConfigService configService, ApplicationScanningService scanningService) {
        this.workingDir = workingDir;
        this.configService = configService;
        this.scanningService = scanningService;
        this.requestHandlers = new LinkedList<>();
    }

    /**
     * Manually loads request handlers that are otherwise loaded using reflection in the real
     * javache web server.
     */
    @Override
    public void loadRequestHandlers(List<String> list) {

        this.requestHandlers.add(
                new SoletDispatcher(
                        this.workingDir,
                        new ApplicationLoadingServiceImpl(
                                this.scanningService,
                                this.configService,
                                this.workingDir + configService.getConfigParam(ConfigConstants.ASSETS_DIR_NAME, String.class)
                        ),
                        this.configService
                ));

        this.requestHandlers.add(new ResourceHandler(workingDir, s -> Collections.singletonList(""), this.configService));
    }

    @Override
    public List<RequestHandler> getRequestHandlers() {
        return this.requestHandlers;
    }

}
