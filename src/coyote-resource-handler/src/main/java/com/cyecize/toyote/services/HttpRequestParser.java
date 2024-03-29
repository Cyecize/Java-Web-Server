package com.cyecize.toyote.services;

import com.cyecize.http.HttpRequest;
import com.cyecize.javache.api.RequestHandlerSharedData;
import com.cyecize.toyote.exceptions.CannotParseRequestException;

import java.io.InputStream;

public interface HttpRequestParser {
    HttpRequest parseHttpRequest(InputStream inputStream, RequestHandlerSharedData sharedData) throws CannotParseRequestException;
}
