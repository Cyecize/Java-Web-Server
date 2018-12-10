package com.cyecize.toyote;

import com.cyecize.http.*;
import com.cyecize.javache.ConfigConstants;
import com.cyecize.javache.api.RequestHandler;
import com.cyecize.javache.io.Writer;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.toyote.services.AppNameCollector;
import com.cyecize.toyote.services.AppNameCollectorImpl;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ResourceHandler implements RequestHandler {

    private static final String RESOURCE_NOT_FOUND_MESSAGE = "<h1 style=\"text-align: center;\">The resource - \"%s\" you are looking for cannot be found.</h1>";

    private final String serverRootFolderPath;

    private final JavacheConfigService configService;

    private boolean hasIntercepted;

    private AppNameCollector appNameCollector;

    private String webappsDirName;

    private String assetsDirName;

    private String classesDirName;

    private String mainAppName;

    public ResourceHandler(String serverRootFolderPath, JavacheConfigService configService) {
        this(serverRootFolderPath, new AppNameCollectorImpl(configService.getConfigParam(ConfigConstants.WEB_APPS_DIR_NAME, String.class)), configService);
    }

    public ResourceHandler(String serverRootFolderPath, AppNameCollector appNameCollector, JavacheConfigService configService) {
        this.serverRootFolderPath = serverRootFolderPath;
        this.appNameCollector = appNameCollector;
        this.configService = configService;
        this.hasIntercepted = false;
        this.initDirectories();
        System.out.println("Loaded Toyote");
    }

    /**
     * Iterates all application names and finds the one that the requestUrl starts with
     * or returns ROOT if no app name is matched.
     */
    private synchronized String getApplicationName(String requestUrl) {
        List<String> applicationNames = this.appNameCollector.getApplicationNames(this.serverRootFolderPath);
        for (String applicationName : applicationNames) {
            if (requestUrl.startsWith(applicationName) && applicationName.length() > 0) {
                return applicationName.substring(1);
            }
        }
        return this.mainAppName;
    }

    private String getResourceName(String requestUrl, String appName) {
        return requestUrl.replace("/" + appName, "");
    }

    /**
     * Handles resource not found response.
     */
    private void notFound(String resourceName, HttpResponse response) {
        response.setStatusCode(HttpStatus.NOT_FOUND);
        response.addHeader("Content-Type", "text/html");
        response.setContent(String.format(RESOURCE_NOT_FOUND_MESSAGE, resourceName));
    }

    /**
     * Tries to read a file and set its content to the HttpResponse.
     * If exception is thrown, handle resource not found.
     */
    private boolean handleResourceRequest(String resourcesFolder, String resourceName, HttpResponse response) {
        try {
            File file = new File(resourcesFolder + File.separator + resourceName);
            Path resourcePath = Paths.get(new URL("file:/" + file.getCanonicalPath()).toURI());
            byte[] resourceContent = this.readAllBytes(file);

            response.setStatusCode(HttpStatus.OK);

            response.addHeader("Content-Type", Files.probeContentType(resourcePath));
            response.addHeader("Content-Length", resourceContent.length + "");
            response.addHeader("Content-Disposition", "inline");

            response.setContent(resourceContent);
            return true;
        } catch (IOException | URISyntaxException e) {
            this.notFound(resourceName, response);
            return false;
        }
    }

    /**
     * Creates HttpRequest and HttpResponse.
     * Generates resource folder.
     * Writes the response content.
     */
    @Override
    public void handleRequest(byte[] inputStream, OutputStream outputStream) {
        try {
            HttpRequest request = new HttpRequestImpl(new String(inputStream, StandardCharsets.UTF_8));
            HttpResponse response = new HttpResponseImpl();

            String applicationName = this.getApplicationName(request.getRequestURL());
            String resourceName = this.getResourceName(request.getRequestURL(), applicationName);

            String resourcesFolder = this.serverRootFolderPath
                    + this.webappsDirName
                    + applicationName
                    + File.separator
                    + this.classesDirName;

            if (!this.handleResourceRequest(resourcesFolder, resourceName, response)) {
                resourcesFolder = this.serverRootFolderPath
                        + this.assetsDirName
                        + applicationName;
                this.handleResourceRequest(resourcesFolder, resourceName, response);
            }

            new Writer().writeBytes(response.getBytes(), outputStream);
            response = null;
            this.hasIntercepted = true;
        } catch (IOException e) {
            e.printStackTrace();
            this.hasIntercepted = false;
        }
        System.gc();
    }

    @Override
    public boolean hasIntercepted() {
        return this.hasIntercepted;
    }

    /**
     * Reads file bytes.
     */
    private byte[] readAllBytes(File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            return in.readAllBytes();
        }
    }

    private void initDirectories() {
        this.webappsDirName = this.configService.getConfigParam(ConfigConstants.WEB_APPS_DIR_NAME, String.class);
        this.assetsDirName = this.configService.getConfigParam(ConfigConstants.ASSETS_DIR_NAME, String.class);
        this.classesDirName = this.configService.getConfigParam(ConfigConstants.APP_COMPILE_OUTPUT_DIR_NAME, String.class);
        this.mainAppName = this.configService.getConfigParam(ConfigConstants.MAIN_APP_JAR_NAME, String.class);
    }
}
