/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket;

/**
 * @author Jared Wiltshire
 */
public class WebSocketClosedException extends WebSocketSendException {
    private static final long serialVersionUID = 1L;

    public WebSocketClosedException() {
        super("Websocket closed");
    }

    public WebSocketClosedException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebSocketClosedException(String message) {
        super(message);
    }

    public WebSocketClosedException(Throwable cause) {
        super(cause);
    }
}
