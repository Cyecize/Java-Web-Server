package com.cyecize.http;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class HttpSessionStorageImpl implements HttpSessionStorage {

    private final ConcurrentHashMap<String, HttpSession> sessions;

    public HttpSessionStorageImpl() {
        this.sessions = new ConcurrentHashMap<>();
    }

    @Override
    public void addSession(HttpSession session) {
        this.sessions.putIfAbsent(session.getId(), session);
    }

    @Override
    public void refreshSessions() {
        final List<String> invalidSessions = this.sessions.entrySet().stream()
                .filter(kvp -> !kvp.getValue().isValid())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        invalidSessions.forEach(this.sessions::remove);
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
