package com.cyecize.summer.areas.security.interfaces;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.HttpSoletResponse;
import com.cyecize.summer.areas.security.models.SecurityConfig;

public interface SecurityConfigHandler {
    void handle(HttpSoletRequest request, HttpSoletResponse response, SecurityConfig config);
}
