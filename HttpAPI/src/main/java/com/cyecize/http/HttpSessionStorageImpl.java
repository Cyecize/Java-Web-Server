package com.cyecize.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpSessionStorageImpl implements HttpSessionStorage {

    private Map<String, HttpSession> sessions;

    public HttpSessionStorageImpl() {
        this.sessions = new HashMap<>();
    }

    @Override
    public void addSession(HttpSession session) {
        this.sessions.putIfAbsent(session.getId(), session);
    }

    @Override
    public void refreshSessions() {
        this.sessions = this.sessions.entrySet().stream()
                .filter(kvp -> kvp.getValue().isValid())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public HttpSession getSession(String sessionId) {
        return this.sessions.get(sessionId);
    }

    @Override
    public Map<String, HttpSession> getAllSessions() {
        return Collections.unmodifiableMap(this.sessions);
    }
}
