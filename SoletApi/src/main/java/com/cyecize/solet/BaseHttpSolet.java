package com.cyecize.solet;

import com.cyecize.http.HttpStatus;

public abstract class BaseHttpSolet implements HttpSolet {
    private boolean isInitialized;

    private SoletConfig soletConfig;

    protected BaseHttpSolet() {
        this.isInitialized = false;
    }

    private void configureNotFound(HttpSoletRequest request, HttpSoletResponse response) {
        response.setStatusCode(HttpStatus.NOT_FOUND);

        response.addHeader("Content-Type", "text/html");
    }

    protected void doGet(HttpSoletRequest request, HttpSoletResponse response) {
        this.configureNotFound(request, response);

        response.setContent((
                "<h1>[ERROR] GET "
                        + request.getRequestURL()
                        + "</h1><br/>"
                        + "<h3>[MESSAGE] The page or functionality you are looking for is not found.</h3>"
        ).getBytes());
    }

    protected void doPost(HttpSoletRequest request, HttpSoletResponse response) {
        this.configureNotFound(request, response);

        response.setContent((
                "<h1>[ERROR] POST "
                        + request.getRequestURL()
                        + "</h1><br/>"
                        + "<h3>[MESSAGE] The page or functionality you are looking for is not found.</h3>"
        ).getBytes());
    }

    protected void doPut(HttpSoletRequest request, HttpSoletResponse response) {
        this.configureNotFound(request, response);

        response.setContent((
                "<h1>[ERROR] PUT "
                        + request.getRequestURL()
                        + "</h1><br/>"
                        + "<h3>[MESSAGE] The page or functionality you are looking for is not found.</h3>"
        ).getBytes());
    }

    protected void doDelete(HttpSoletRequest request, HttpSoletResponse response) {
        this.configureNotFound(request, response);

        response.setContent((
                "<h1>[ERROR] DELETE "
                        + request.getRequestURL()
                        + "</h1><br/>"
                        + "<h3>[MESSAGE] The page or functionality you are looking for is not found.</h3>"
        ).getBytes());
    }

    @Override
    public void init(SoletConfig soletConfig) {
        this.isInitialized = true;
        this.soletConfig = soletConfig;
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
    public void service(HttpSoletRequest request, HttpSoletResponse response) {
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