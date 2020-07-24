package com.cyecize.broccolina.services;

import com.cyecize.http.HttpResponse;
import com.cyecize.http.HttpSessionStorage;
import com.cyecize.solet.HttpSoletRequest;

public interface SessionManagementService {

    void initSessionIfExistent(HttpSoletRequest request);

    void sendSessionIfExistent(HttpSoletRequest request, HttpResponse response);

    void clearInvalidSessions();

    HttpSessionStorage getSessionStorage();
}
