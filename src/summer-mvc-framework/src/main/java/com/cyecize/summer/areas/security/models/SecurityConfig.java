package com.cyecize.summer.areas.security.models;

import com.cyecize.summer.constants.IocConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SecurityConfig {

    private String loginURL;

    private String unauthorizedURL;

    private String logoutURL;

    private String logoutRedirectURL;

    private List<SecuredArea> securedAreas;

    public SecurityConfig() {
        this.initSecuredAreas();
    }

    public SecurityConfig setLoginURL(String loginURL) {
        this.loginURL = loginURL;
        return this;
    }

    public String getLoginURL() {
        return loginURL;
    }

    public SecurityConfig setUnauthorizedURL(String unauthorizedURL) {
        this.unauthorizedURL = unauthorizedURL;
        return this;
    }

    public String getUnauthorizedURL() {
        return unauthorizedURL;
    }

    public SecurityConfig setLogoutURL(String logoutURL) {
        this.logoutURL = logoutURL;
        return this;
    }

    public String getLogoutURL() {
        return logoutURL;
    }

    public SecurityConfig setLogoutRedirectURL(String logoutRedirectURL) {
        this.logoutRedirectURL = logoutRedirectURL;
        return this;
    }

    public String getLogoutRedirectURL() {
        return logoutRedirectURL;
    }

    public SecurityConfig addSecuredArea(SecuredArea securedArea) {
        this.securedAreas.add(securedArea);
        return this;
    }

    public List<SecuredArea> getSecuredAreas() {
        return securedAreas;
    }

    /**
     * Init secured area and add META-INF to the list with a role that is unique and will never exist
     * so that no user can access the directory.
     */
    private void initSecuredAreas() {
        this.securedAreas = new ArrayList<>();
        this.securedAreas.add(new SecuredArea("/META-INF", UUID.randomUUID().toString()));
        this.securedAreas.add(new SecuredArea("/" + IocConstants.PROPERTIES_FILE_NAME, UUID.randomUUID().toString()));
    }
}
