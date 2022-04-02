package com.cyecize.summer.areas.security.interceptors;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.HttpSoletResponse;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.security.annotations.PreAuthorize;
import com.cyecize.summer.areas.security.enums.AuthorizationType;
import com.cyecize.summer.areas.security.exceptions.NoSecurityConfigurationException;
import com.cyecize.summer.areas.security.models.Principal;
import com.cyecize.summer.areas.security.models.SecuredArea;
import com.cyecize.summer.areas.security.models.SecurityConfig;
import com.cyecize.summer.common.annotations.Component;
import com.cyecize.summer.common.annotations.Optional;
import com.cyecize.summer.common.enums.ServiceLifeSpan;
import com.cyecize.summer.common.extensions.InterceptorAdapter;

@Component(lifespan = ServiceLifeSpan.REQUEST)
public class SecurityInterceptor implements InterceptorAdapter {

    private static final String NO_SECURITY_CONFIG_MSG = SecurityConfig.class.getName()
            + " configuration is missing, create one in a bean!";

    private final SecurityConfig securityConfig;

    private final Principal principal;

    public SecurityInterceptor(@Optional SecurityConfig securityConfig, Principal principal) {
        this.securityConfig = securityConfig;
        this.principal = principal;
    }

    @Override
    public boolean preHandle(HttpSoletRequest request, HttpSoletResponse response, Object handler) {
        if (this.securityConfig != null) {
            if (request.getRelativeRequestURL().equals(this.securityConfig.getLogoutURL())) {
                this.securityConfig.getBeforeLogoutHandler().handle(request, response, this.securityConfig);
                this.principal.logout();
                this.securityConfig.getLogoutCompletedHandler().handle(request, response, this.securityConfig);
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

        preAuthorize = actionMethod.getController().getServiceType().getAnnotation(PreAuthorize.class);
        if (preAuthorize != null) {
            return this.handleAnnotation(preAuthorize, request, response);
        }

        return true;
    }

    /**
     * Scans the secured areas.
     * Calls the corresponding error handlers if one of the following conditions is met:
     * If Route matches secured area, check if the user is logged in and call error handler if not.
     * If the user is logged in, check if the required role is present and call error handler not present.
     */
    private boolean handleSecuredAreas(HttpSoletRequest request, HttpSoletResponse response) {
        final SecuredArea securedArea = this.securityConfig.getSecuredAreas().stream()
                .filter(sa -> sa.getRoute().matcher(request.getRelativeRequestURL()).find())
                .findFirst().orElse(null);

        if (securedArea == null) {
            return true;
        }

        if (!principal.isUserPresent()) {
            this.securityConfig.getNotLoggedInHandler().handle(request, response, this.securityConfig);
            return false;
        }

        if (!this.principal.hasAuthority(securedArea.getAuthority())) {
            this.securityConfig.getNotPrivilegedHandler().handle(request, response, this.securityConfig);
        }

        return true;
    }

    /**
     * Perform security checks and call the corresponding error handler if any one of the following conditions is met:
     * If User is not logged in and authorization is required
     * If User is logged in, but Anonymous is required
     * If User is logged, and role is required, check if the user has that role and call error handler role is absent.
     */
    private boolean handleAnnotation(PreAuthorize annotation, HttpSoletRequest request, HttpSoletResponse response) {
        if (this.securityConfig == null) throw new NoSecurityConfigurationException(NO_SECURITY_CONFIG_MSG);

        if (annotation.value() == AuthorizationType.LOGGED_IN && !this.principal.isUserPresent()) {
            this.securityConfig.getNotLoggedInHandler().handle(request, response, this.securityConfig);
            return false;
        }

        if (annotation.value() == AuthorizationType.ANONYMOUS) {
            if (this.principal.isUserPresent()) {
                this.securityConfig.getNotPrivilegedHandler().handle(request, response, this.securityConfig);
                return false;
            }

            return true;
        }

        if (!annotation.role().equals("")) {
            if (!this.principal.hasAuthority(annotation.role())) {
                this.securityConfig.getNotPrivilegedHandler().handle(request, response, this.securityConfig);
                return false;
            }
        }

        return true;
    }

    @Override
    public int getOrder() {
        if (this.securityConfig != null) {
            return this.securityConfig.getSecurityInterceptorOrder();
        }

        return InterceptorAdapter.super.getOrder();
    }
}
