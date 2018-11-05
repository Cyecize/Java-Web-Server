package com.cyecize.broccolina.services;

import com.cyecize.http.HttpRequest;
import com.cyecize.http.HttpResponse;

public interface SessionManagementService {

    void initSessionIfExistent(HttpRequest request);

    void sendSessionIfExistent(HttpRequest request, HttpResponse response);

    void clearInvalidSessions();
}
