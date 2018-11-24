package com.cyecize.solet;

import com.cyecize.http.HttpStatus;

public abstract class BaseHttpSolet implements HttpSolet {

    private static final String FUNCTIONALITY_NOT_FOUND_FORMAT = "<h1>[ERROR] %s %s </h1><br/>"
            + "<h3>[MESSAGE] The page or functionality you are looking for is not found.</h3>";

    private boolean isInitialized;

    private SoletConfig soletConfig;

    protected String appNamePrefix;

    protected String assetsFolder;

    protected BaseHttpSolet() {
        this.isInitialized = false;
        this.appNamePrefix = "";
    }

    private void configureNotFound(HttpSoletRequest request, HttpSoletResponse response) {
        response.setStatusCode(HttpStatus.NOT_FOUND);
        response.addHeader("Content-Type", "text/html");
    }

    protected String createRoute(String route) {
        return this.appNamePrefix + route;
    }

    protected void doGet(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        this.configureNotFound(request, response);
        response.setContent((String.format(FUNCTIONALITY_NOT_FOUND_FORMAT, "GET", request.getRequestURL())));
    }

    protected void doPost(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        this.configureNotFound(request, response);
        response.setContent((String.format(FUNCTIONALITY_NOT_FOUND_FORMAT, "POST", request.getRequestURL())));
    }

    protected void doPut(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        this.configureNotFound(request, response);
        response.setContent((String.format(FUNCTIONALITY_NOT_FOUND_FORMAT, "PUT", request.getRequestURL())));
    }

    protected void doDelete(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        this.configureNotFound(request, response);
        response.setContent((String.format(FUNCTIONALITY_NOT_FOUND_FORMAT, "DELETE", request.getRequestURL())));
    }

    @Override
    public void init(SoletConfig soletConfig) {
        this.isInitialized = true;
        this.soletConfig = soletConfig;
    }

    @Override
    public void setAppNamePrefix(String appName) {
        this.appNamePrefix = appName;
    }

    @Override
    public void setAssetsFolder(String dir) {
        this.assetsFolder = dir;
    }

    @Override
    public boolean isInitialized() {
        return this.isInitialized;
    }

    @Override
    public SoletConfig getSoletConfig() {
        return this.soletConfig;
    }

    @Override
    public void service(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        switch (request.getMethod().toUpperCase()) {
            case "GET":
                this.doGet(request, response);
                break;
            case "POST":
                this.doPost(request, response);
                break;
            case "PUT":
                this.doPut(request, response);
                break;
            case "DELETE":
                this.doDelete(request, response);
                break;
        }
    }
}