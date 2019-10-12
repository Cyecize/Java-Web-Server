package com.cyecize.toyote.services;

import com.cyecize.http.HttpResponse;
import com.cyecize.http.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;

public interface ErrorHandlingService {
    boolean handleException(OutputStream outputStream, Throwable throwable, HttpResponse response) throws IOException;

    boolean handleException(OutputStream outputStream, Throwable throwable, HttpResponse response, HttpStatus status) throws IOException;
}
