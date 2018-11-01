package com.cyecize.http;

import java.util.Arrays;
import java.util.Map;

import static com.cyecize.http.Constants.SERVER_HTTP_VERSION;


public interface HttpResponse {

    enum ResponseLines {
        OK (SERVER_HTTP_VERSION
                + " "
                + HttpStatus.OK.getStatusPhrase()),
        CREATED (SERVER_HTTP_VERSION
                + " "
                + HttpStatus.CREATED.getStatusPhrase()),
        NO_CONTENT (SERVER_HTTP_VERSION
                + " "
                + HttpStatus.NO_CONTENT.getStatusPhrase()),
        SEE_OTHER (SERVER_HTTP_VERSION
                + " "
                + HttpStatus.SEE_OTHER.getStatusPhrase()),
        BAD_REQUEST (SERVER_HTTP_VERSION
                + " "
                + HttpStatus.BAD_REQUEST.getStatusPhrase()),
        UNAUTHORIZED (SERVER_HTTP_VERSION
                + " "
                + HttpStatus.UNAUTHORIZED.getStatusPhrase()),
        FORBIDDEN (SERVER_HTTP_VERSION
                + " "
                + HttpStatus.FORBIDDEN.getStatusPhrase()),
        NOT_FOUND (SERVER_HTTP_VERSION
                + " "
                + HttpStatus.NOT_FOUND.getStatusPhrase()),
        INTERNAL_SERVER_ERROR (SERVER_HTTP_VERSION
                + " "
                + HttpStatus.INTERNAL_SERVER_ERROR.getStatusPhrase());

        private String value;

        ResponseLines(String responseLine) {
            this.value = responseLine;
        }

        static String getResponseLine(int statusCode) {
            return Arrays.stream(values()).filter(entry -> entry.value.contains(statusCode + "")).findFirst().orElse(INTERNAL_SERVER_ERROR).value;
        }
    }

    void setStatusCode(HttpStatus statusCode);

    void setContent(byte[] content);

    void addHeader(String header, String value);

    void addCookie(String name, String value);

    void addCookie(HttpCookie cookie);

    byte[] getContent();

    byte[] getBytes();

    Map<String, String> getHeaders();

    HttpStatus getStatusCode();

}
