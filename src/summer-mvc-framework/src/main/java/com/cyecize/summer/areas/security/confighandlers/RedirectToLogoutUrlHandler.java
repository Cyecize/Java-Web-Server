package com.cyecize.summer.areas.security.confighandlers;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.HttpSoletResponse;
import com.cyecize.summer.areas.security.interfaces.SecurityConfigHandler;
import com.cyecize.summer.areas.security.models.SecurityConfig;

public class RedirectToLogoutUrlHandler implements SecurityConfigHandler {
    @Override
    public void handle(HttpSoletRequest request, HttpSoletResponse response, SecurityConfig config) {
        response.sendRedirect(request.getContextPath() + config.getLogoutRedirectURL());
    }
}
