/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket;

import javax.servlet.ServletContext;

import org.eclipse.jetty.websocket.api.WebSocketBehavior;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.server.WebSocketServerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.jetty.JettyRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import com.serotonin.m2m2.Common;

/**
 *
 * @author Terry Packer
 */
@Component("mangoWebSocketHandshakeHandlerV2")
public class MangoWebSocketHandshakeHandler extends DefaultHandshakeHandler {
    private static final int KB = 1024;
    private static final long SECOND = 1000L;

    public MangoWebSocketHandshakeHandler(@Autowired ServletContext servletContext) {
        super(new JettyRequestUpgradeStrategy(new WebSocketServerFactory(servletContext, getPolicy())));
    }

    static WebSocketPolicy getPolicy() {
        int inputBufferSize = Common.envProps.getInt("web.websocket.inputBufferSize", 8 * KB);
        int maxTextMessageSize = Common.envProps.getInt("web.websocket.maxTextMessageSize", 64 * KB);
        int maxTextMessageBufferSize = Common.envProps.getInt("web.websocket.maxTextMessageBufferSize", 32 * KB);
        int maxBinaryMessageSize = Common.envProps.getInt("web.websocket.maxBinaryMessageSize", 64 * KB);
        int maxBinaryMessageBufferSize = Common.envProps.getInt("web.websocket.maxBinaryMessageBufferSize", 32 * KB);
        long asyncWriteTimeout = Common.envProps.getLong("web.websocket.asyncWriteTimeoutMs", 60 * SECOND);
        long idleTimeout = Common.envProps.getLong("web.websocket.idleTimeoutMs", 60 * SECOND);

        WebSocketPolicy policy = new WebSocketPolicy(WebSocketBehavior.SERVER);
        policy.setInputBufferSize(inputBufferSize);
        policy.setMaxTextMessageSize(maxTextMessageSize);
        policy.setMaxTextMessageBufferSize(maxTextMessageBufferSize);
        policy.setMaxBinaryMessageSize(maxBinaryMessageSize);
        policy.setMaxBinaryMessageBufferSize(maxBinaryMessageBufferSize);
        policy.setAsyncWriteTimeout(asyncWriteTimeout);

        // ping pong mechanism will keep socket alive, web.websocket.pingTimeoutMs should be set lower than the idle timeout
        policy.setIdleTimeout(idleTimeout);
        return policy;
    }
}
