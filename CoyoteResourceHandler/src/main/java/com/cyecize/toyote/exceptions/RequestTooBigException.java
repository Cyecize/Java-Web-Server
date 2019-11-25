package com.cyecize.toyote.exceptions;

public class RequestTooBigException extends RuntimeException {

    private final int contentLength;

    public RequestTooBigException(String message, int contentLength) {
        super(message);
        this.contentLength = contentLength;
    }

    public int getContentLength() {
        return this.contentLength;
    }
}
