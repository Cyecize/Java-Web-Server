package com.cyecize.toyote;

import com.cyecize.http.*;
import com.cyecize.javache.api.RequestHandler;
import com.cyecize.javache.io.Writer;
import com.cyecize.toyote.services.AppNameCollector;
import com.cyecize.toyote.services.AppNameCollectorImpl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ResourceHandler implements RequestHandler {
    private static final String APPLICATION_RESOURCES_FOLDER_NAME = "resources";

    private static final String RESOURCE_NOT_FOUND_MESSAGE = "<h1 style=\"text-align: center;\">The resource - \"%s\" you are looking for cannot be found.</h1>";

    private final String serverRootFolderPath;

    private boolean hasIntercepted;

    private AppNameCollector appNameCollector;

    public ResourceHandler(String serverRootFolderPath) {
        this.serverRootFolderPath = serverRootFolderPath;
        this.hasIntercepted = false;
        this.appNameCollector = new AppNameCollectorImpl();
        System.out.println("Loaded Toyote");
    }

    private String getApplicationName(String requestUrl) {
        List<String> applicationNames = this.appNameCollector.getApplicationNames(this.serverRootFolderPath);
        for (String applicationName : applicationNames) {
            if (requestUrl.startsWith(applicationName)) {
                return applicationName.substring(1);
            }
        }
        return "ROOT";
    }

    private String getResourceName(String requestUrl, String appName) {
        return requestUrl.replace("/" + appName, "");
    }

    private void notFound(String resourceName, HttpResponse response) {
        response.setStatusCode(HttpStatus.NOT_FOUND);
        response.addHeader("Content-Type", "text/html");
        response.setContent(String.format(RESOURCE_NOT_FOUND_MESSAGE, resourceName).getBytes());
    }

    private void handleResourceRequest(String resourcesFolder, String resourceName, HttpResponse response) {
        try {
            Path resourcePath = Paths.get(new URL("file:/" + new File(resourcesFolder + File.separator + resourceName).getCanonicalPath()).toURI());
            byte[] resourceContent = Files.readAllBytes(resourcePath);

            response.setStatusCode(HttpStatus.OK);

            response.addHeader("Content-Type", Files.probeContentType(resourcePath));
            response.addHeader("Content-Length", resourceContent.length + "");
            response.addHeader("Content-Disposition", "inline");

            response.setContent(resourceContent);
        } catch (IOException | URISyntaxException e) {
            this.notFound(resourceName, response);
        }
    }

    @Override
    public void handleRequest(String s, OutputStream outputStream) {
        try {
            HttpRequest request = new HttpRequestImpl(s);
            HttpResponse response = new HttpResponseImpl();

            String applicationName = this.getApplicationName(request.getRequestURL());
            String resourcesFolder = this.serverRootFolderPath
                    + "webapps"
                    + File.separator
                    + applicationName
                    + File.separator
                    + APPLICATION_RESOURCES_FOLDER_NAME;

            String resourceName = this.getResourceName(request.getRequestURL(), applicationName);

            this.handleResourceRequest(resourcesFolder, resourceName, response);

            new Writer().writeBytes(response.getBytes(), outputStream);
            this.hasIntercepted = true;
        } catch (IOException e) {
            e.printStackTrace();
            this.hasIntercepted = false;
        }
    }

    @Override
    public boolean hasIntercepted() {
        return this.hasIntercepted;
    }
}
