package com.cyecize.broccolina;

import com.cyecize.broccolina.services.*;
import com.cyecize.http.HttpStatus;
import com.cyecize.javache.api.RequestHandler;
import com.cyecize.javache.io.Writer;
import com.cyecize.solet.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoletDispatcher implements RequestHandler {

    private static final String APPLICATIONS_FOLDER_NAME = "webapps/";

    private static final String ASSETS_FOLDER_NAME = "assets/";

    private final String workingDir;

    private Map<String, HttpSolet> soletMap;

    private List<String> applicationNames;

    private ApplicationLoadingService applicationLoadingService;

    private SessionManagementService sessionManagementService;

    private boolean hasIntercepted;

    private String currentRequestAppName;

    public SoletDispatcher(String workingDir) {
        this.workingDir = workingDir;
        this.hasIntercepted = false;
        this.applicationLoadingService = new ApplicationLoadingServiceImpl(new JarFileUnzipServiceImpl(),
                this.workingDir + APPLICATIONS_FOLDER_NAME, this.workingDir + ASSETS_FOLDER_NAME);
        this.sessionManagementService = new SessionManagementServiceImpl();
        this.initializeSoletMap();
        this.currentRequestAppName = "";
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] bytes = inputStream.readAllBytes();

        HttpSoletRequest request = new HttpSoletRequestImpl(new String(bytes, StandardCharsets.UTF_8), new ByteArrayInputStream(bytes));
        HttpSoletResponse response = new HttpSoletResponseImpl(outputStream);
        this.resolveCurrentRequestAppName(request);

        this.sessionManagementService.initSessionIfExistent(request);
        HttpSolet solet = this.findSoletCandidate(request);
        if (solet == null /*|| request.isResource()*/) {
            this.hasIntercepted = false;
            return;
        }

        if (!this.runSolet(solet, request, response)) {
            this.hasIntercepted = false;
            return;
        }

        if (response.getStatusCode() == null) {
            response.setStatusCode(HttpStatus.OK);
        }

        this.sessionManagementService.sendSessionIfExistent(request, response);
        this.sessionManagementService.clearInvalidSessions();
        new Writer().writeData(response.getResponse(), response.getOutputStream());
        response = null;
        this.hasIntercepted = true;
    }

    @Override
    public boolean hasIntercepted() {
        return this.hasIntercepted;
    }

    private synchronized boolean runSolet(HttpSolet solet, HttpSoletRequest request, HttpSoletResponse response) {
        try {
            solet.service(request, response);
            return solet.hasIntercepted();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        return true;
    }

    private void resolveCurrentRequestAppName(HttpSoletRequest request) {
        boolean isAppNameFound = false;
        for (String applicationName : this.applicationNames) {
            if (request.getRequestURL().startsWith(applicationName)) {
                this.currentRequestAppName = applicationName;
                isAppNameFound = true;
                break;
            }
        }
        if (!isAppNameFound) {
            this.currentRequestAppName = "";
        }
        request.setContextPath(this.currentRequestAppName);
    }

    /**
     * Search by constant route, then by regex and finally look for "/*" type routes.
     */
    private HttpSolet findSoletCandidate(HttpSoletRequest request) {
        String requestUrl = request.getRequestURL();
        Pattern applicationRouteMatchPattern = Pattern.compile(Pattern.quote(this.currentRequestAppName) + "\\/[a-zA-Z0-9]+\\/");
        Matcher applicationRouteMatcher = applicationRouteMatchPattern.matcher(requestUrl);

        if (this.soletMap.containsKey(requestUrl)) {
            return this.soletMap.get(requestUrl);
        }

        if (applicationRouteMatcher.find()) {
            String applicationRoute = applicationRouteMatcher.group(0) + "*";
            if (this.soletMap.containsKey(applicationRoute)) {
                return this.soletMap.get(applicationRoute);
            }
        }

        if (this.soletMap.containsKey(this.currentRequestAppName + "/*")) {
            return this.soletMap.get(this.currentRequestAppName + "/*");
        }

        return null;
    }

    private void initializeSoletMap() {
        try {
            this.soletMap = this.applicationLoadingService.loadApplications();
            this.applicationNames = this.applicationLoadingService.getApplicationNames();
            System.out.println("Loaded Applications: " + String.join(", ", this.applicationNames));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
