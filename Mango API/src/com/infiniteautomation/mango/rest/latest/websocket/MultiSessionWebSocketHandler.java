/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jared Wiltshire
 */
public abstract class MultiSessionWebSocketHandler extends MangoWebSocketHandler {

    protected final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    public MultiSessionWebSocketHandler() {
        super();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        super.afterConnectionClosed(session, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        sessions.remove(session);
        super.handleTransportError(session, exception);
    }

}
