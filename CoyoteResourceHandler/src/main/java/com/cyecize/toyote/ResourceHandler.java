package com.cyecize.toyote;

import com.cyecize.http.*;
import com.cyecize.javache.api.RequestHandler;
import com.cyecize.javache.io.Writer;
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

    private static final String WEB_APPS_DIR_NAME = "webapps";

    private static final String WEB_ASSETS_DIR_NAME = "assets";

    private static final String CLASSES_FOLDER_NAME = "classes";

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
        response.setContent(String.format(RESOURCE_NOT_FOUND_MESSAGE, resourceName));
    }

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

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream) {
        try {
            HttpRequest request = new HttpRequestImpl(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
            HttpResponse response = new HttpResponseImpl();

            String applicationName = this.getApplicationName(request.getRequestURL());
            String resourceName = this.getResourceName(request.getRequestURL(), applicationName);
            String resourcesFolder = this.serverRootFolderPath
                    + WEB_APPS_DIR_NAME
                    + File.separator
                    + applicationName
                    + File.separator
                    + CLASSES_FOLDER_NAME;

            if (!this.handleResourceRequest(resourcesFolder, resourceName, response)) {
                resourcesFolder = this.serverRootFolderPath
                        + WEB_ASSETS_DIR_NAME
                        + File.separator
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

    private byte[] readAllBytes(File file) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        while (true) {
            int nBytes = in.read(buffer);
            if (nBytes <= 0) {
                break;
            }
            out.write(buffer, 0, nBytes);
        }
        return out.toByteArray();
    }
}
