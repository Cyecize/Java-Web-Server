package com.cyecize.toyote.services;

import com.cyecize.http.HttpResponse;
import com.cyecize.http.HttpStatus;
import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.toyote.exceptions.RequestTooBigException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Service for displaying exception messages to the browser.
 */
@Service
public class ErrorHandlingServiceImpl implements ErrorHandlingService {

    private boolean printStackTrace;

    @Autowired
    public ErrorHandlingServiceImpl(JavacheConfigService configService) {
        this.printStackTrace = configService.getConfigParam(JavacheConfigValue.JAVACHE_PRINT_EXCEPTIONS, boolean.class);
    }

    @Override
    public boolean handleRequestTooBig(OutputStream outputStream, RequestTooBigException ex, HttpResponse response)
            throws IOException {
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        this.writeException(outputStream, ex, response);

        return true;
    }

    @Override
    public boolean handleException(OutputStream outputStream, Throwable throwable, HttpResponse response)
            throws IOException {
        return this.handleException(outputStream, throwable, response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public boolean handleException(OutputStream outputStream, Throwable throwable,
                                   HttpResponse response, HttpStatus status) throws IOException {
        if (!this.printStackTrace) {
            return false;
        }

        response.setStatusCode(status);
        this.writeException(outputStream, throwable, response);

        return true;
    }

    private void writeException(OutputStream outputStream,
                                Throwable throwable, HttpResponse response) throws IOException {
        final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        throwable.printStackTrace(new PrintStream(byteOutputStream));

        response.setContent(byteOutputStream.toByteArray());

        outputStream.write(response.getBytes());
    }
}
