package com.cyecize.broccolina;

import com.cyecize.broccolina.services.*;
import com.cyecize.http.HttpRequest;
import com.cyecize.http.HttpResponse;
import com.cyecize.http.HttpStatus;
import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.api.IoC;
import com.cyecize.javache.api.RequestHandler;
import com.cyecize.javache.api.RequestHandlerSharedData;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.solet.*;

import java.io.*;

@Service
public class SoletDispatcher implements RequestHandler {

    private final JavacheConfigService configService;

    private final ApplicationLoadingService applicationLoadingService;

    private final SessionManagementService sessionManagementService;

    private final SoletCandidateFinder soletCandidateFinder;

    private final boolean trackResources;

    @Autowired
    public SoletDispatcher(ApplicationLoadingService applicationLoadingService, JavacheConfigService configService, SessionManagementService sessionManagementService, SoletCandidateFinder soletCandidateFinder) {
        this.configService = configService;
        this.applicationLoadingService = applicationLoadingService;
        this.sessionManagementService = sessionManagementService;
        this.soletCandidateFinder = soletCandidateFinder;
        this.trackResources = configService.getConfigParam(JavacheConfigValue.BROCCOLINA_TRACK_RESOURCES, boolean.class);
    }

    @Override
    public void init() {
        try {
            this.soletCandidateFinder.init(
                    this.applicationLoadingService.loadApplications(this.createSoletConfig()),
                    this.applicationLoadingService.getApplicationNames()
            );

            System.out.println("Loaded Applications: " + String.join(", ", this.applicationLoadingService.getApplicationNames()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean handleRequest(InputStream inputStream, OutputStream outputStream, RequestHandlerSharedData sharedData) throws IOException {
        final HttpSoletRequest request = new HttpSoletRequestImpl(
                sharedData.getObject(BroccolinaConstants.SHARED_DATA_HTTP_REQUEST_KEY, HttpRequest.class)
        );

        final HttpSoletResponse response = new HttpSoletResponseImpl(
                sharedData.getObject(BroccolinaConstants.SHARED_DATA_HTTP_RESPONSE_KEY, HttpResponse.class)
        );

        this.sessionManagementService.initSessionIfExistent(request);
        final HttpSolet solet = this.soletCandidateFinder.findSoletCandidate(request);

        if (solet == null || (request.isResource() && !this.trackResources)) {
            return false;
        }

        if (!this.runSolet(solet, request, response)) {
            return false;
        }

        if (response.getStatusCode() == null) {
            response.setStatusCode(HttpStatus.OK);
        }

        this.sessionManagementService.sendSessionIfExistent(request, response);
        this.sessionManagementService.clearInvalidSessions();

        outputStream.write(response.getBytes());

        return true;
    }

    @Override
    public int order() {
        return this.configService.getConfigParam(JavacheConfigValue.BROCCOLINA_SOLET_DISPATCHER_ORDER, int.class);
    }

    private boolean runSolet(HttpSolet solet, HttpSoletRequest request, HttpSoletResponse response) {
        try {
            solet.service(request, response);
            return solet.hasIntercepted();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }

    /**
     * Create SoletConfig instance and add objects.
     * This Solet Config will be used for initializing every solet.
     */
    private SoletConfig createSoletConfig() {
        SoletConfig soletConfig = new SoletConfigImpl();
        soletConfig.setAttribute(BroccolinaConstants.SOLET_CONFIG_SESSION_STORAGE_KEY, this.sessionManagementService.getSessionStorage());
        soletConfig.setAttribute(BroccolinaConstants.SOLET_CONFIG_SERVER_CONFIG_SERVICE_KEY, this.configService);
        soletConfig.setAttribute(BroccolinaConstants.SOLET_CONFIG_DEPENDENCY_CONTAINER_KEY, IoC.getRequestHandlersDependencyContainer());
        //TODO add more items here
        return soletConfig;
    }
}
