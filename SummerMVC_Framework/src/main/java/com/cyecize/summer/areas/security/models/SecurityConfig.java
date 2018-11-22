package com.cyecize.summer.areas.security.models;

public class SecurityConfig {

    private String loginURL;

    private String unauthorizedURL;

    private String logoutURL;

    private String logoutRedirectURL;

    public SecurityConfig() {

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
}
