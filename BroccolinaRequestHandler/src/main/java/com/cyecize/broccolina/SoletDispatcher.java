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

    private static final String TEMP_FOLDER_NAME = "temp/";

    private final String workingDir;

    private final JavacheConfigService configService;

    private final ApplicationLoadingService applicationLoadingService;

    private String tempDir;

    private String rootAppName;

    private boolean showRequestContent;

    private SessionManagementService sessionManagementService;

    private Map<String, HttpSolet> soletMap;

    private List<String> applicationNames;

    private boolean hasIntercepted;

    private String currentRequestAppName;

    public SoletDispatcher(String workingDir, JavacheConfigService configService) {
        this(
                workingDir,
                new ApplicationLoadingServiceImpl(
                        new ApplicationScanningServiceImpl(workingDir, new JarFileUnzipServiceImpl(), configService),
                        configService,
                        workingDir + configService.getConfigParam(ConfigConstants.ASSETS_DIR_NAME, String.class)
                ),
                configService
        );
    }

    public SoletDispatcher(String workingDir, ApplicationLoadingService applicationLoadingService, JavacheConfigService configService) {
        this.workingDir = workingDir;
        this.configService = configService;
        this.applicationLoadingService = applicationLoadingService;
        this.sessionManagementService = new SessionManagementServiceImpl();
        this.rootAppName = configService.getConfigParam(ConfigConstants.MAIN_APP_JAR_NAME, String.class);
        this.hasIntercepted = false;
        this.initializeSoletMap();
        this.currentRequestAppName = "";
        this.initTempDir();
        this.showRequestContent = configService.getConfigParam(ConfigConstants.SHOW_REQUEST_LOG, boolean.class);
    }

    /**
     * Creates temporaryStorageService
     * Creates HttpSoletRequest and HttpSoletResponse.
     * Resolves the current request's app name.
     * <p>
     * Initializes the session.
     * Finds a matching solet and runs it.
     * If no solet is found or solet has not intercepted, sets the hasIntercepted field to false.
     * <p>
     * Sends a session cookie.
     * Clears invalid sessions.
     * Writes the response.
     * <p>
     * Finally removes any temp files if available.
     */
    @Override
    public void handleRequest(byte[] bytes, OutputStream outputStream) throws IOException {
        TemporaryStorageService temporaryStorageService = new TemporaryStorageServiceImpl(this.tempDir);

        try {
            HttpSoletRequest request = new HttpSoletRequestImpl(this.extractRequestContent(bytes), bytes, temporaryStorageService);
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
            request = null;
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
    private String extractRequestContent(byte[] bytes) {
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
        if (this.showRequestContent) {
            System.out.println(requestContent);
        }

        return requestContent;
    }

    /**
     * This method is synchronized because otherwise There is an interference with the request and response objects.
     * Calls the run method of the solet
     * returns whether the solet has intercepted the request.
     */
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

    /**
     * Checks if the request's route starts with any of the loaded application names.
     * If that is the case, set the currentRequestAppName to the matching appName.
     * Otherwise set the currentRequestAppName to ""
     */
    private void resolveCurrentRequestAppName(HttpSoletRequest request) {
        boolean isAppNameFound = false;
        for (String applicationName : this.applicationNames) {
            if (request.getRequestURL().startsWith(applicationName) && !applicationName.equals(this.rootAppName)) {
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

    /**
     * Gets all available solets and applicationNames.
     * Prints the loaded applications.
     */
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

    /**
     * Create javache's temporary directory if it doesn't exist.
     */
    private void initTempDir() {
        this.tempDir = this.workingDir + TEMP_FOLDER_NAME;

        File workingDir = new File(this.tempDir);
        if (!workingDir.exists()) {
            workingDir.mkdir();
        }
    }
}
