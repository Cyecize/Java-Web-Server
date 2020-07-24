package com.cyecize.solet;

import com.cyecize.http.HttpStatus;

public abstract class BaseHttpSolet implements HttpSolet {

    private boolean isInitialized;

    private boolean hasIntercepted;

    private SoletConfig soletConfig;

    protected BaseHttpSolet() {
        this.isInitialized = false;
        this.setHasIntercepted(true);
    }

    /**
     * Create proper route having the app name in mind.
     *
     * @param route - required route.
     * @return formatted route.
     */
    protected String createRoute(String route) {
        return this.soletConfig.getAttribute(SoletConstants.SOLET_CONFIG_APP_NAME_PREFIX) + route;
    }

    protected void doGet(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        this.functionalityNotFound(request, response);
    }

    protected void doPost(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        this.functionalityNotFound(request, response);
    }

    protected void doPut(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        this.functionalityNotFound(request, response);
    }

    protected void doDelete(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        this.functionalityNotFound(request, response);
    }

    protected void setHasIntercepted(boolean hasIntercepted) {
        this.hasIntercepted = hasIntercepted;
    }

    @Override
    public void init(SoletConfig soletConfig) {
        this.soletConfig = soletConfig;
        this.soletConfig.setIfMissing(SoletConstants.SOLET_CONFIG_APP_NAME_PREFIX, "");
        this.isInitialized = true;
    }

    @Override
    public boolean isInitialized() {
        return this.isInitialized;
    }

    @Override
    public boolean hasIntercepted() {
        return this.hasIntercepted;
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
            default:
                this.functionalityNotFound(request, response);
                break;
        }
    }

    private void functionalityNotFound(HttpSoletRequest request, HttpSoletResponse response) {
        response.setStatusCode(HttpStatus.NOT_IMPLEMENTED);
        response.addHeader("Content-Type", "text/html");
        response.setContent((String.format(
                "<h1>[ERROR] %s %s </h1><br/><h3>[MESSAGE] The page or functionality you are looking for is not found.</h3>",
                request.getMethod(),
                request.getRequestURL()))
        );
    }
}