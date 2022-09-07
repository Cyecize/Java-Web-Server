package com.cyecize.broccolina;

import com.cyecize.broccolina.services.ApplicationLoadingService;
import com.cyecize.broccolina.services.SessionManagementService;
import com.cyecize.broccolina.services.SoletCandidateFinder;
import com.cyecize.http.HttpRequest;
import com.cyecize.http.HttpResponse;
import com.cyecize.http.HttpStatus;
import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.api.IoC;
import com.cyecize.javache.api.RequestHandler;
import com.cyecize.javache.api.RequestHandlerSharedData;
import com.cyecize.javache.api.SharedDataPropertyNames;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.javache.services.LoggingService;
import com.cyecize.solet.HttpSolet;
import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.HttpSoletRequestImpl;
import com.cyecize.solet.HttpSoletResponse;
import com.cyecize.solet.HttpSoletResponseImpl;
import com.cyecize.solet.SoletConfig;
import com.cyecize.solet.SoletConfigImpl;
import com.cyecize.solet.SoletConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Request handler responsible for processing dynamic HTTP requests.
 * It will look for a solet with mapping that matches the request URL and will forward the request
 * content to the given solet.
 */
@Service
public class SoletDispatcher implements RequestHandler {

    private final JavacheConfigService configService;

    private final ApplicationLoadingService applicationLoadingService;

    private final SessionManagementService sessionManagementService;

    private final SoletCandidateFinder soletCandidateFinder;

    private final LoggingService loggingService;

    private final boolean trackResources;

    @Autowired
    public SoletDispatcher(ApplicationLoadingService applicationLoadingService, JavacheConfigService configService,
                           SessionManagementService sessionManagementService, SoletCandidateFinder soletCandidateFinder,
                           LoggingService loggingService) {
        this.configService = configService;
        this.applicationLoadingService = applicationLoadingService;
        this.sessionManagementService = sessionManagementService;
        this.soletCandidateFinder = soletCandidateFinder;
        this.trackResources = configService.getConfigParam(JavacheConfigValue.BROCCOLINA_TRACK_RESOURCES, boolean.class);
        this.loggingService = loggingService;
    }

    @Override
    public void init() {
        try {
            this.soletCandidateFinder.init(
                    this.applicationLoadingService.loadApplications(this.createSoletConfig()),
                    this.applicationLoadingService.getApplicationNames()
            );

            this.loggingService.info("Loaded Applications: " +
                    String.join(", ", this.applicationLoadingService.getApplicationNames())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean handleRequest(InputStream inputStream, OutputStream outputStream, RequestHandlerSharedData sharedData)
            throws IOException {
        final HttpSoletRequest request = new HttpSoletRequestImpl(
                sharedData.getObject(SharedDataPropertyNames.HTTP_REQUEST, HttpRequest.class)
        );

        final HttpSoletResponse response = new HttpSoletResponseImpl(
                sharedData.getObject(SharedDataPropertyNames.HTTP_RESPONSE, HttpResponse.class),
                outputStream
        );

        if (request.isResource() && !this.trackResources) {
            return false;
        }

        this.sessionManagementService.initSessionIfExistent(request);
        final HttpSolet solet = this.soletCandidateFinder.findSoletCandidate(request);

        if (solet == null || !this.runSolet(solet, request, response)) {
            return false;
        }

        if (response.getStatusCode() == null) {
            response.setStatusCode(HttpStatus.OK);
        }

        this.sessionManagementService.sendSessionIfExistent(request, response);
        this.sessionManagementService.clearInvalidSessions();

        response.getOutputStream().write();

        return true;
    }

    /**
     * Order if this request handler is configurable.
     *
     * @return the order of the request handler.
     */
    @Override
    public int order() {
        return this.configService.getConfigParam(JavacheConfigValue.BROCCOLINA_SOLET_DISPATCHER_ORDER, int.class);
    }

    private boolean runSolet(HttpSolet solet, HttpSoletRequest request, HttpSoletResponse response) {
        try {
            solet.service(request, response);
            return solet.hasIntercepted();
        } catch (Exception ex) {
            this.loggingService.printStackTrace(ex);
        }

        return true;
    }

    /**
     * Create SoletConfig instance and add objects.
     * This Solet Config will be used for initializing every solet.
     */
    private SoletConfig createSoletConfig() {
        final SoletConfig soletConfig = new SoletConfigImpl();
        soletConfig.setAttribute(
                SoletConstants.SOLET_CONFIG_SESSION_STORAGE_KEY,
                this.sessionManagementService.getSessionStorage()
        );

        soletConfig.setAttribute(
                SoletConstants.SOLET_CONFIG_SERVER_CONFIG_SERVICE_KEY,
                this.configService
        );

        soletConfig.setAttribute(
                SoletConstants.SOLET_CONFIG_DEPENDENCY_CONTAINER_KEY,
                IoC.getRequestHandlersDependencyContainer()
        );

        return soletConfig;
    }
}
