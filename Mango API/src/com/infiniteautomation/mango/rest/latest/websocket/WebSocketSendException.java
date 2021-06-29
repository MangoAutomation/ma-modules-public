/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket;

/**
 * @author Jared Wiltshire
 */
public class WebSocketSendException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public WebSocketSendException() {
        super();
    }

    public WebSocketSendException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebSocketSendException(String message) {
        super(message);
    }

    public WebSocketSendException(Throwable cause) {
        super(cause);
    }
}
