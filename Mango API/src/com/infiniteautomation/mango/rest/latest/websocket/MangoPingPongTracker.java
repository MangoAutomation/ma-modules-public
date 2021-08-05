/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Class to test and disconnect WebSocket sessions if the Client goes away without notifying us.
 * Saves Ping/Pong state in Session attributes.
 *
 * @author Terry Packer
 * @author Jared Wiltshire
 */
public class MangoPingPongTracker implements Runnable {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final WebSocketSession session;

    protected MangoPingPongTracker(WebSocketSession session) {
        this.session = session;
    }

    @Override
    public void run() {
        Boolean receivedPong = (Boolean) this.session.getAttributes().put(MangoWebSocketHandler.RECEIVED_PONG, Boolean.FALSE);
        if (receivedPong == null || receivedPong) {
            this.sendPing();
        } else {
            this.closeSession(new CloseStatus(CloseStatus.SESSION_NOT_RELIABLE.getCode(), "Didn't receive pong message within timeout period"));
        }
    }

    private void sendPing() {
        if (this.session.isOpen()) {
            try {
                session.sendMessage(new PingMessage());
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("Error sending ping message", e);
                }
                this.closeSession(new CloseStatus(CloseStatus.SESSION_NOT_RELIABLE.getCode(), "Error sending ping message"));
            }
        }
    }

    private void closeSession(CloseStatus status) {
        try {
            session.close(status);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Error closing WebSocket session", e);
            }
        }
    }
}