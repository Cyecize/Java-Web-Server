package com.cyecize.summer.areas.security.interceptors;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.HttpSoletResponse;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.security.annotations.PreAuthorize;
import com.cyecize.summer.areas.security.enums.AuthorizationType;
import com.cyecize.summer.areas.security.exceptions.NoSecurityConfigurationException;
import com.cyecize.summer.areas.security.exceptions.UnauthorizedException;
import com.cyecize.summer.areas.security.models.Principal;
import com.cyecize.summer.areas.security.models.SecuredArea;
import com.cyecize.summer.areas.security.models.SecurityConfig;
import com.cyecize.summer.common.annotations.Component;
import com.cyecize.summer.common.annotations.Optional;
import com.cyecize.summer.common.enums.ServiceLifeSpan;
import com.cyecize.summer.common.extensions.InterceptorAdapter;

@Component(lifespan = ServiceLifeSpan.REQUEST)
public class SecurityInterceptor implements InterceptorAdapter {

    private static final String NO_SECURITY_CONFIG_MSG = SecurityConfig.class.getName() + " configuration is missing, create one in a bean!";

    private static final String NOT_AUTHORIZED_FOR_URL_FORMAT = "User not authorized for \"%s\".";

    private final SecurityConfig securityConfig;

    private final Principal principal;

    public SecurityInterceptor(@Optional SecurityConfig securityConfig, Principal principal) {
        this.securityConfig = securityConfig;
        this.principal = principal;
    }

    @Override
    public boolean preHandle(HttpSoletRequest request, HttpSoletResponse response, Object handler) throws Exception {

        if (this.securityConfig != null) {
            if (request.getRelativeRequestURL().equals(this.securityConfig.getLogoutURL())) {
                this.principal.logout();
                response.sendRedirect(request.getContextPath() + this.securityConfig.getLogoutRedirectURL());
                return false;
            }

            if (!this.handleSecuredAreas(request, response)) {
                return false;
            }
        }

        if (!(handler instanceof ActionMethod)) {
            return true;
        }

        final ActionMethod actionMethod = (ActionMethod) handler;

        PreAuthorize preAuthorize = actionMethod.getMethod().getAnnotation(PreAuthorize.class);
        if (preAuthorize != null) {
            return this.handleAnnotation(preAuthorize, request, response);
        }

        preAuthorize = actionMethod.getControllerClass().getAnnotation(PreAuthorize.class);
        if (preAuthorize != null) {
            return this.handleAnnotation(preAuthorize, request, response);
        }

        return true;
    }

    /**
     * Scans the secured areas.
     * If Route matches secured area, check if the user is logged in and redirect to login if not.
     * If the user is logged in, check if the required role is present and throw Exception if it not present.
     */
    private boolean handleSecuredAreas(HttpSoletRequest request, HttpSoletResponse response) throws UnauthorizedException {
        final SecuredArea securedArea = this.securityConfig.getSecuredAreas().stream()
                .filter(sa -> sa.getRoute().matcher(request.getRelativeRequestURL()).find())
                .findFirst().orElse(null);

        if (securedArea == null) {
            return true;
        }

        if (!principal.isUserPresent()) {
            this.handleNotLoggedIn(request, response);
            return false;
        }

        if (!this.principal.hasAuthority(securedArea.getAuthority())) {
            this.handleNotPrivileged(request, response);
        }

        return true;
    }

    /**
     * If User is not logged in and authorization is required, redirect to login with refer callback.
     * If User is logged in, but Anonymous is required, consider it as HTTP 401.
     * If User is logged, and role is required, check if the user has that role and Handle HTTP 401 if role is absent.
     */
    private boolean handleAnnotation(PreAuthorize annotation, HttpSoletRequest request, HttpSoletResponse response)
            throws Exception {
        if (this.securityConfig == null) throw new NoSecurityConfigurationException(NO_SECURITY_CONFIG_MSG);

        if (annotation.value() == AuthorizationType.LOGGED_IN && !this.principal.isUserPresent()) {
            this.handleNotLoggedIn(request, response);
            return false;
        }

        if (annotation.value() == AuthorizationType.ANONYMOUS) {
            if (this.principal.isUserPresent()) {
                this.handleNotPrivileged(request, response);
                return false;
            }

            return true;
        }

        if (!annotation.role().equals("")) {
            if (!this.principal.hasAuthority(annotation.role())) {
                this.handleNotPrivileged(request, response);
                return false;
            }
        }

        return true;
    }

    private void handleNotLoggedIn(HttpSoletRequest request, HttpSoletResponse response) {
        response.sendRedirect(
                request.getContextPath() + this.securityConfig.getLoginURL() +
                        "?callback=" + request.getRelativeRequestURL()
        );
    }

    private void handleNotPrivileged(HttpSoletRequest request, HttpSoletResponse response) throws UnauthorizedException {
        throw new UnauthorizedException(String.format(NOT_AUTHORIZED_FOR_URL_FORMAT, request.getRelativeRequestURL()));
    }
}
