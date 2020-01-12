package com.cyecize.http;

import java.util.Arrays;

import static com.cyecize.http.Constants.SERVER_HTTP_VERSION;

public enum HttpStatus {
    OK(200, "OK"),

    CREATED(201, "Created"),

    ACCEPTED(202, "Accepted"),

    NO_CONTENT(204, "No Content"),

    MOVED_PERMANENTLY(301, "Moved Permanently"),

    FOUND(302, "Found"),

    SEE_OTHER(303, "See Other"),

    BAD_REQUEST(400, "Bad Request"),

    UNAUTHORIZED(401, "Unauthorized"),

    FORBIDDEN(403, "Forbidden"),

    NOT_FOUND(404, "Not Found"),

    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),

    PAYLOAD_TOO_LARGE(413, "Payload Too Large"),

    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),

    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),

    NOT_IMPLEMENTED(501, "Not Implemented");

    private final int statusCode;

    private final String statusPhrase;

    HttpStatus(int statusCode, String statusPhrase) {
        this.statusCode = statusCode;
        this.statusPhrase = statusPhrase;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public String getStatusPhrase() {
        return this.statusPhrase;
    }

    public static String getResponseLine(int statusCode) {
        final HttpStatus httpStatus = Arrays.stream(values())
                .filter(sc -> sc.statusCode == statusCode)
                .findFirst().orElse(INTERNAL_SERVER_ERROR);

        return SERVER_HTTP_VERSION
                + " "
                + httpStatus.getStatusCode()
                + " "
                + httpStatus.getStatusPhrase();
    }
}
