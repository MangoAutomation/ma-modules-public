/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.websocket;

/**
 * @author Jared Wiltshire
 */
public class WebSocketClosedException extends WebSocketSendException {
    private static final long serialVersionUID = 1L;

    public WebSocketClosedException() {
        super();
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
