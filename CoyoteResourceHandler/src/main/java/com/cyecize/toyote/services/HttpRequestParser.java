package com.cyecize.toyote.services;

import com.cyecize.http.HttpRequest;

import java.io.IOException;
import java.io.InputStream;

public interface HttpRequestParser {
    HttpRequest parseHttpRequest(InputStream inputStream) throws IOException;
}
