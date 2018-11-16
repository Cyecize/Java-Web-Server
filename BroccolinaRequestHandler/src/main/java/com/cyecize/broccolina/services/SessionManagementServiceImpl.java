package com.cyecize.broccolina.services;

import com.cyecize.http.*;

import java.util.Date;

public class SessionManagementServiceImpl implements SessionManagementService {

    private static final String SESSION_COOKIE_NAME = "JAVACHE_SESSION_ID";

    private HttpSessionStorage sessionStorage;

    public SessionManagementServiceImpl() {
        this.sessionStorage = new HttpSessionStorageImpl();
    }

    @Override
    public void initSessionIfExistent(HttpRequest request) {
        HttpCookie cookie = request.getCookies().get(SESSION_COOKIE_NAME);
        if (cookie != null) {
            HttpSession session = this.sessionStorage.getSession(cookie.getValue());
            if (session != null && session.isValid()) {
                request.setSession(session);
            } else {
                request.getCookies().remove(SESSION_COOKIE_NAME);
                request.setSession(new HttpSessionImpl());
            }
        } else {
            request.setSession(new HttpSessionImpl());
        }
    }

    @Override
    public void sendSessionIfExistent(HttpRequest request, HttpResponse response) {
        if (request.getSession() == null) {
            return;
        }

        if (this.sessionStorage.getSession(request.getSession().getId()) == null) {
            this.sessionStorage.addSession(request.getSession());
        }

        if (request.getSession().isValid()) {
            HttpCookie cookie = new HttpCookieImpl(SESSION_COOKIE_NAME, request.getSession().getId());
            cookie.setPath("/");
            response.addCookie(cookie);
        } else {
            response.addCookie(SESSION_COOKIE_NAME, "removed; expires=" + new Date(0).toString());
        }

    }

    @Override
    public void clearInvalidSessions() {
        this.sessionStorage.refreshSessions();
    }
}
