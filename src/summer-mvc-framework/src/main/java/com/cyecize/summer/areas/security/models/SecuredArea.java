package com.cyecize.summer.areas.security.models;

import java.util.regex.Pattern;

public class SecuredArea {

    private static final String REGEX_START = "^";

    private static final String REGEX_MATCH_ALL = ".*";

    private Pattern route;

    private String authority;

    public SecuredArea(String route, String authority) {
        this.setRoute(route);
        this.setAuthority(authority);
    }

    public Pattern getRoute() {
        return this.route;
    }

    private void setRoute(String route) {
        if (route == null) {
            throw new IllegalArgumentException("Route is null!");
        }
        this.route = Pattern.compile(REGEX_START + Pattern.quote(route) + REGEX_MATCH_ALL);
    }

    public String getAuthority() {
        return this.authority;
    }

    private void setAuthority(String authority) {
        if (authority == null) {
            throw new IllegalArgumentException("Authority is null!");
        }
        this.authority = authority;
    }
}
