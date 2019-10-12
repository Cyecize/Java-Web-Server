package com.cyecize.toyote.services;

import com.cyecize.http.HttpResponse;
import com.cyecize.http.HttpStatus;
import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.services.JavacheConfigService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

@Service
public class ErrorHandlingServiceImpl implements ErrorHandlingService {

    private boolean printStackTrace;

    @Autowired
    public ErrorHandlingServiceImpl(JavacheConfigService configService) {
        this.printStackTrace = configService.getConfigParam(JavacheConfigValue.JAVACHE_PRINT_EXCEPTIONS, boolean.class);
    }

    @Override
    public boolean handleException(OutputStream outputStream, Throwable throwable, HttpResponse response) throws IOException {
        return this.handleException(outputStream, throwable, response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public boolean handleException(OutputStream outputStream, Throwable throwable, HttpResponse response, HttpStatus status) throws IOException {
        if (!this.printStackTrace) {
            return false;
        }

        final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        throwable.printStackTrace(new PrintStream(byteOutputStream));

        response.setStatusCode(status);
        response.setContent(byteOutputStream.toByteArray());

        outputStream.write(response.getBytes());

        return true;
    }
}
