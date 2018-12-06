package com.cyecize.broccolina;

import com.cyecize.broccolina.services.*;
import com.cyecize.http.HttpStatus;
import com.cyecize.javache.ConfigConstants;
import com.cyecize.javache.api.RequestHandler;
import com.cyecize.javache.io.Writer;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.solet.*;
import com.cyecize.solet.service.TemporaryStorageService;
import com.cyecize.solet.service.TemporaryStorageServiceImpl;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoletDispatcher implements RequestHandler {

    private static final String APPLICATIONS_FOLDER_NAME = "webapps/";

    private static final String ASSETS_FOLDER_NAME = "assets/";

    private static final String TEMP_FOLDER_NAME = "temp/";

    private final String workingDir;

    private String tempDir;

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
        this.initTempDir();
    }

    @Override
    public void handleRequest(byte[] bytes, OutputStream outputStream, JavacheConfigService config) throws IOException {
        TemporaryStorageService temporaryStorageService = new TemporaryStorageServiceImpl(this.tempDir);

        try {
            HttpSoletRequest request = new HttpSoletRequestImpl(this.extractRequestContent(bytes, config), bytes, temporaryStorageService);
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
        } finally {
            temporaryStorageService.removeTemporaryFiles();
        }
    }

    @Override
    public boolean hasIntercepted() {
        return this.hasIntercepted;
    }

    /**
     * Filter larger requests with Multipart Encoding to save memory
     * by getting only the first 2048 bytes and leaving the rest to the multipart parser.
     */
    private String extractRequestContent(byte[] bytes, JavacheConfigService configService) {
        String requestContent = "";
        if (bytes.length <= 2048) {
            requestContent = new String(bytes, StandardCharsets.UTF_8);
        } else {
            requestContent = new String(Arrays.copyOf(bytes, 2048), StandardCharsets.UTF_8);
            if (!requestContent.contains("Content-Type: multipart")) {
                requestContent = new String(bytes, StandardCharsets.UTF_8);
            }
        }

        //print the content if the SHOW_REQUEST_LOG is set to true
        if (configService.getConfigParam(ConfigConstants.SHOW_REQUEST_LOG, boolean.class)) {
            System.out.println(requestContent);
        }

        return requestContent;
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

    private void initTempDir() {
        this.tempDir = this.workingDir + TEMP_FOLDER_NAME;

        File workingDir = new File(this.tempDir);
        if (!workingDir.exists()) {
            workingDir.mkdir();
        }
    }
}
