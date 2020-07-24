package com.cyecize.toyote.services;

import com.cyecize.http.HttpResponse;
import com.cyecize.http.HttpStatus;
import com.cyecize.toyote.exceptions.RequestTooBigException;

import java.io.IOException;
import java.io.OutputStream;

public interface ErrorHandlingService {

    boolean handleRequestTooBig(OutputStream outputStream, RequestTooBigException ex, HttpResponse response) throws IOException;

    boolean handleException(OutputStream outputStream, Throwable throwable, HttpResponse response) throws IOException;

    boolean handleException(OutputStream outputStream, Throwable throwable, HttpResponse response, HttpStatus status) throws IOException;
}
