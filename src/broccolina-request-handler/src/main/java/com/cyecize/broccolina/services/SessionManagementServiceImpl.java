package com.cyecize.broccolina.services;

import com.cyecize.broccolina.BroccolinaConstants;
import com.cyecize.http.HttpCookie;
import com.cyecize.http.HttpCookieImpl;
import com.cyecize.http.HttpResponse;
import com.cyecize.http.HttpSession;
import com.cyecize.http.HttpSessionImpl;
import com.cyecize.http.HttpSessionStorage;
import com.cyecize.http.HttpSessionStorageImpl;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.solet.HttpSoletRequest;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
public class SessionManagementServiceImpl implements SessionManagementService {

    private static final String SESSION_COOKIE_NAME = "JAVACHE_SESSION_ID";

    private final HttpSessionStorage sessionStorage;

    public SessionManagementServiceImpl() {
        this.sessionStorage = new HttpSessionStorageImpl();
    }

    /**
     * Gets looks for a cookie representing the sessionId.
     * If the cookie is not present, adds a new session to the HttpRequest.
     * If the cookie is present, checks if a session with that id exists.
     * If no session exists or the session is not valid,
     * removes the cookie and a new session to the HttpRequest.
     * if the session is valid, sets the session to the HttpRequest.
     */
    @Override
    public void initSessionIfExistent(HttpSoletRequest request) {
        final HttpCookie cookie = request.getCookies().get(this.getSessionCookieName(request));

        if (cookie != null) {
            final HttpSession session = this.sessionStorage.getSession(cookie.getValue());
            if (this.isSessionValid(session)) {
                request.setSession(session);
            } else {
                request.getCookies().remove(SESSION_COOKIE_NAME);
                this.addNewSession(request);
            }
        } else {
            this.addNewSession(request);
        }
    }

    /**
     * If the session is new, adds it to the sessionStorage map.
     * If the session is valid, adds a cookie.
     * If the session is invalid, removes the cookie.
     */
    @Override
    public void sendSessionIfExistent(HttpSoletRequest request, HttpResponse response) {
        if (request.getSession() == null) {
            return;
        }

        if (this.sessionStorage.getSession(request.getSession().getId()) == null) {
            this.sessionStorage.addSession(request.getSession());
        }

        if (request.getSession().isValid()) {
            final HttpCookie cookie = new HttpCookieImpl(this.getSessionCookieName(request), request.getSession().getId());

            final String expires = DateTimeFormatter.RFC_1123_DATE_TIME.format(
                    ZonedDateTime.now().plusDays(BroccolinaConstants.SESSION_EXPIRE_DAYS)
            );

            cookie.setPath("/; expires=" + expires);

            response.addCookie(cookie);
        } else {
            response.addCookie(SESSION_COOKIE_NAME, "removed; expires=" + new Date(0).toString());
        }
    }

    @Override
    public void clearInvalidSessions() {
        this.sessionStorage.refreshSessions();
    }

    @Override
    public HttpSessionStorage getSessionStorage() {
        return this.sessionStorage;
    }

    private void addNewSession(HttpSoletRequest request) {
        request.setSession(new HttpSessionImpl());
    }

    private boolean isSessionValid(HttpSession session) {
        return session != null && session.isValid();
    }

    private String getSessionCookieName(HttpSoletRequest request) {
        return SESSION_COOKIE_NAME + request.getContextPath();
    }
}
