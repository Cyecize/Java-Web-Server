package com.cyecize.summer.areas.security.models;

import com.cyecize.summer.areas.security.confighandlers.RedirectToLoginUrlHandler;
import com.cyecize.summer.areas.security.confighandlers.RedirectToLogoutUrlHandler;
import com.cyecize.summer.areas.security.confighandlers.RelaxedSecurityConfigHandler;
import com.cyecize.summer.areas.security.confighandlers.ThrowUnauthorizedExceptionHandler;
import com.cyecize.summer.areas.security.interfaces.SecurityConfigHandler;
import com.cyecize.summer.constants.IocConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SecurityConfig {

    private int securityInterceptorOrder = 0;

    private String loginURL;

    private SecurityConfigHandler notLoggedInHandler = new RedirectToLoginUrlHandler();

    private SecurityConfigHandler notPrivilegedHandler = new ThrowUnauthorizedExceptionHandler();

    private String logoutURL;

    private String logoutRedirectURL;

    private SecurityConfigHandler beforeLogoutHandler = new RelaxedSecurityConfigHandler();

    private SecurityConfigHandler logoutCompletedHandler = new RedirectToLogoutUrlHandler();

    private List<SecuredArea> securedAreas;

    public SecurityConfig() {
        this.initSecuredAreas();
    }

    public SecurityConfig setSecurityInterceptorOrder(int securityInterceptorOrder) {
        this.securityInterceptorOrder = securityInterceptorOrder;
        return this;
    }

    public int getSecurityInterceptorOrder() {
        return securityInterceptorOrder;
    }

    public SecurityConfig setLoginURL(String loginURL) {
        this.loginURL = loginURL;
        return this;
    }

    public String getLoginURL() {
        return loginURL;
    }

    public SecurityConfig setNotLoggedInHandler(SecurityConfigHandler notLoggedInHandler) {
        if (notLoggedInHandler == null) {
            throw new IllegalArgumentException("Security Config Handler must not be null!");
        }

        this.notLoggedInHandler = notLoggedInHandler;
        return this;
    }

    public SecurityConfigHandler getNotLoggedInHandler() {
        return notLoggedInHandler;
    }

    public SecurityConfig setNotPrivilegedHandler(SecurityConfigHandler notPrivilegedHandler) {
        if (notPrivilegedHandler == null) {
            throw new IllegalArgumentException("Security Config Handler must not be null!");
        }
        this.notPrivilegedHandler = notPrivilegedHandler;
        return this;
    }

    public SecurityConfigHandler getNotPrivilegedHandler() {
        return notPrivilegedHandler;
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

    public SecurityConfig setBeforeLogoutHandler(SecurityConfigHandler beforeLogoutHandler) {
        if (beforeLogoutHandler == null) {
            throw new IllegalArgumentException("Security Config Handler must not be null!");
        }
        this.beforeLogoutHandler = beforeLogoutHandler;
        return this;
    }

    public SecurityConfigHandler getBeforeLogoutHandler() {
        return beforeLogoutHandler;
    }

    public SecurityConfig setLogoutCompletedHandler(SecurityConfigHandler logoutCompletedHandler) {
        if (logoutCompletedHandler == null) {
            throw new IllegalArgumentException("Security Config Handler must not be null!");
        }
        this.logoutCompletedHandler = logoutCompletedHandler;
        return this;
    }

    public SecurityConfigHandler getLogoutCompletedHandler() {
        return logoutCompletedHandler;
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
