package com.cyecize.toyote;

import com.cyecize.http.*;
import com.cyecize.javache.ConfigConstants;
import com.cyecize.javache.api.RequestHandler;
import com.cyecize.javache.io.Writer;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.toyote.models.CachedFile;
import com.cyecize.toyote.services.AppNameCollector;
import com.cyecize.toyote.services.AppNameCollectorImpl;
import com.cyecize.toyote.services.FileCachingService;
import com.cyecize.toyote.services.FileCachingServiceImpl;

import java.io.*;
import java.net.SocketException;
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

    private final FileCachingService cachingService;

    private boolean hasIntercepted;

    private AppNameCollector appNameCollector;

    private List<String> applicationNames;

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
        this.applicationNames = this.appNameCollector.getApplicationNames(this.serverRootFolderPath);
        this.cachingService = new FileCachingServiceImpl(configService);
        System.out.println("Loaded Toyote");
    }

    /**
     * Iterates all application names and finds the one that the requestUrl starts with
     * or returns ROOT if no app name is matched.
     */
    private String getApplicationName(String requestUrl) {
        for (String applicationName : this.applicationNames) {
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

    private void resolveCacheType(HttpResponse response, String contentType, String resourceName) {
        String finalCacheHeader = "";
        if (contentType == null) {
            return;
        }

        //TODO: add service for handling cache-control header.
        switch (contentType) {
            case "text/plain":
                if (resourceName.endsWith(".js")) {
                    finalCacheHeader = "max-age=60";
                }

                break;
            case "text/css":
                finalCacheHeader = "max-age=120";
                break;
            case "image/x-icon":
                finalCacheHeader = "max-age=180";
                break;
            default:
                finalCacheHeader = "no-cache";
                break;
        }

        response.addHeader("Cache-Control", finalCacheHeader);
    }

    /**
     * Handles resource found response.
     */
    private void found(HttpResponse response, String contentType, byte[] resourceContent, String resourceName) {
        response.setStatusCode(HttpStatus.OK);

        response.addHeader("Content-Type", contentType);
        response.addHeader("Content-Length", resourceContent.length + "");
        response.addHeader("Content-Disposition", "inline");
        this.resolveCacheType(response, contentType, resourceName);

        response.setContent(resourceContent);
    }

    /**
     * Tries to read a file and set its content to the HttpResponse.
     * If exception is thrown, handle resource not found.
     */
    private boolean handleResourceRequest(String resourcesFolder, String resourceName, HttpRequest request, HttpResponse response) throws FileNotFoundException {

        if (this.cachingService.hasCachedFile(resourceName)) {
            CachedFile cachedFile = this.cachingService.getCachedFile(resourceName);
            this.found(response, cachedFile.getContentType(), cachedFile.getFileContent(), resourceName);
            return true;
        }

        try {
            File file = new File(resourcesFolder + File.separator + resourceName);
            Path resourcePath = Paths.get(new URL("file:/" + file.getCanonicalPath()).toURI());
            byte[] resourceContent = this.readAllBytes(file);
            String contentType = Files.probeContentType(resourcePath);
            if (request.getHeaders().containsKey("Content-Type")) {
                contentType = request.getHeaders().get("Content-Type");
            }

            this.cachingService.cacheFile(resourceName, resourceContent, contentType);
            this.found(response, contentType, resourceContent, resourceName);

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

            if (!this.handleResourceRequest(resourcesFolder, resourceName, request, response)) {
                resourcesFolder = this.serverRootFolderPath
                        + this.assetsDirName
                        + applicationName;
                this.handleResourceRequest(resourcesFolder, resourceName, request, response);
            }

            new Writer().writeBytes(response.getBytes(), outputStream);
            response = null;
            request = null;
            this.hasIntercepted = true;
        } catch (IOException e) {
            this.hasIntercepted = false;

            if (e instanceof SocketException) {
                return;
            }

            e.printStackTrace();
        }
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
