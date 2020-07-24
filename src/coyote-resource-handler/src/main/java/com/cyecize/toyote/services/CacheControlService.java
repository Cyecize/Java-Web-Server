package com.cyecize.toyote.services;

import com.cyecize.http.HttpRequest;
import com.cyecize.http.HttpResponse;

public interface CacheControlService {
    void init();

    void addCachingHeader(HttpRequest request, HttpResponse response, String fileMediaType);
}
