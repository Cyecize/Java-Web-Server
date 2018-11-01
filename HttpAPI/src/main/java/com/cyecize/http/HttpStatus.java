package com.cyecize.http;

public enum HttpStatus {
    OK("200 OK"),
    CREATED("201 Created"),
    NO_CONTENT("204 No Content"),
    SEE_OTHER("303 See Other"),
    BAD_REQUEST("400 Bad Request"),
    UNAUTHORIZED("401 Unauthorized"),
    FORBIDDEN("403 Forbidden"),
    NOT_FOUND("404 Not Found"),
    INTERNAL_SERVER_ERROR("500 Internal Server Error");

    private String statusPhrase;

    HttpStatus(String statusPhrase) {
        this.setStatusPhrase(statusPhrase);
    }

    public int getStatusCode() { return Integer.parseInt(this.statusPhrase.split(" ")[0]);}

    public String getStatusPhrase() {
        return this.statusPhrase;
    }

    private void setStatusPhrase(String statusPhrase) {
        this.statusPhrase = statusPhrase;
    }
}
