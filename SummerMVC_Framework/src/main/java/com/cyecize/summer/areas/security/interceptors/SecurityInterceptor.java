package com.cyecize.summer.areas.security.interceptors;

import com.cyecize.http.HttpStatus;
import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.HttpSoletResponse;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.security.annotations.PreAuthorize;
import com.cyecize.summer.areas.security.enums.AuthorizationType;
import com.cyecize.summer.areas.security.exceptions.NoSecurityConfigurationException;
import com.cyecize.summer.areas.security.exceptions.UnauthorizedException;
import com.cyecize.summer.areas.security.models.Principal;
import com.cyecize.summer.areas.security.models.SecurityConfig;
import com.cyecize.summer.common.annotations.Component;
import com.cyecize.summer.common.enums.ServiceLifeSpan;
import com.cyecize.summer.common.extensions.InterceptorAdapter;

@Component(lifespan = ServiceLifeSpan.REQUEST)
public class SecurityInterceptor implements InterceptorAdapter {

    private static final String NO_SECURITY_CONFIG_MSG = SecurityConfig.class.getName() + " configuration is missing, create one in a bean!";

    private static final String NOT_AUTHORIZED_FOR_URL_FORMAT = "User not authorized for \"%s\".";

    private final SecurityConfig securityConfig;

    private final Principal principal;

    public SecurityInterceptor(SecurityConfig securityConfig, Principal principal) {
        this.securityConfig = securityConfig;
        this.principal = principal;
    }

    @Override
    public boolean preHandle(HttpSoletRequest request, HttpSoletResponse response, Object handler) throws Exception {
        if (!(handler instanceof ActionMethod)) {
            System.out.println("handler not an instance");
            return true;
        }

        ActionMethod actionMethod = (ActionMethod) handler;

        PreAuthorize preAuthorize = actionMethod.getMethod().getAnnotation(PreAuthorize.class);
        if (preAuthorize != null) {
            return this.handleAnnotation(preAuthorize, request, response);
        }

        preAuthorize = actionMethod.getControllerClass().getAnnotation(PreAuthorize.class);
        if (preAuthorize != null) {
            return this.handleAnnotation(preAuthorize, request, response);
        }

        if (this.securityConfig != null) {
            System.out.println("req " + request.getRelativeRequestURL());
            System.out.println("actual " + securityConfig.getLogoutURL());
            if (request.getRelativeRequestURL().equals(this.securityConfig.getLogoutURL())) {
                this.principal.logout();
                response.sendRedirect(request.getContextPath() + this.securityConfig.getLogoutRedirectURL());
                return false;
            }
        }

        return true;
    }

    private boolean handleAnnotation(PreAuthorize annotation, HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        if (this.securityConfig == null) throw new NoSecurityConfigurationException(NO_SECURITY_CONFIG_MSG);

        if (annotation.value() == AuthorizationType.LOGGED_IN && !this.principal.isUserPresent()) {
            this.handleNotLoggedIn(request, response);
            return false;
        }
        if (annotation.value() == AuthorizationType.ANONYMOUS) {
            this.handleNotPrivileged(request, response);
            return false;
        }
        if (!annotation.role().equals("")) {
            if (!principal.hasAuthority(annotation.role())) {
                this.handleNotPrivileged(request, response);
                return false;
            }
        }
        return true;
    }

    private void handleNotLoggedIn(HttpSoletRequest request, HttpSoletResponse response) {
        response.sendRedirect(request.getContextPath() + this.securityConfig.getLoginURL() + "?callback=" + request.getRequestURL());
    }

    private void handleNotPrivileged(HttpSoletRequest request, HttpSoletResponse response) throws UnauthorizedException {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        throw new UnauthorizedException(String.format(NOT_AUTHORIZED_FOR_URL_FORMAT, request.getRelativeRequestURL()));
    }
}
