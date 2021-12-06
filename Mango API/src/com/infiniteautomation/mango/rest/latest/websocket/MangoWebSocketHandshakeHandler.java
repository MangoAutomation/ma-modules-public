/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket;

import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import org.eclipse.jetty.websocket.api.WebSocketBehavior;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.server.WebSocketServerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import org.springframework.web.socket.server.jetty.JettyRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/**
 *
 * @author Terry Packer
 */
@Component("mangoWebSocketHandshakeHandlerV2")
public class MangoWebSocketHandshakeHandler extends DefaultHandshakeHandler {

    @Autowired
    public MangoWebSocketHandshakeHandler(ServletContext servletContext, Environment environment) {
        super(new JettyRequestUpgradeStrategy(new WebSocketServerFactory(servletContext, getPolicy(environment))));
    }

    static WebSocketPolicy getPolicy(Environment environment) {
        long inputBufferSize = environment.getProperty("web.websocket.inputBufferSize", long.class, DataSize.of(8, DataUnit.KILOBYTES).toBytes());
        long maxTextMessageSize = environment.getProperty("web.websocket.maxTextMessageSize", long.class, DataSize.of(64, DataUnit.KILOBYTES).toBytes());
        long maxTextMessageBufferSize = environment.getProperty("web.websocket.maxTextMessageBufferSize", long.class, DataSize.of(32, DataUnit.KILOBYTES).toBytes());
        long maxBinaryMessageSize = environment.getProperty("web.websocket.maxBinaryMessageSize", long.class, DataSize.of(64, DataUnit.KILOBYTES).toBytes());
        long maxBinaryMessageBufferSize = environment.getProperty("web.websocket.maxBinaryMessageBufferSize", long.class, DataSize.of(32, DataUnit.KILOBYTES).toBytes());
        long asyncWriteTimeout = environment.getProperty("web.websocket.asyncWriteTimeoutMs", long.class, TimeUnit.SECONDS.toMillis(60));
        long idleTimeout = environment.getProperty("web.websocket.idleTimeoutMs", long.class, TimeUnit.SECONDS.toMillis(60));

        WebSocketPolicy policy = new WebSocketPolicy(WebSocketBehavior.SERVER);
        policy.setInputBufferSize((int) inputBufferSize);
        policy.setMaxTextMessageSize((int) maxTextMessageSize);
        policy.setMaxTextMessageBufferSize((int) maxTextMessageBufferSize);
        policy.setMaxBinaryMessageSize((int) maxBinaryMessageSize);
        policy.setMaxBinaryMessageBufferSize((int) maxBinaryMessageBufferSize);
        policy.setAsyncWriteTimeout(asyncWriteTimeout);

        // ping pong mechanism will keep socket alive, web.websocket.pingTimeoutMs should be set lower than the idle timeout
        policy.setIdleTimeout(idleTimeout);
        return policy;
    }
}
