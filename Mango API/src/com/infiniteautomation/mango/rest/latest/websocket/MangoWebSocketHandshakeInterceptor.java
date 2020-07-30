/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket;

import java.security.Principal;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.serotonin.m2m2.vo.permission.PermissionHolder;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.serotonin.m2m2.vo.User;

/**
 * @author Jared Wiltshire
 */
@Component("mangoWebSocketHandshakeInterceptorV2")
public class MangoWebSocketHandshakeInterceptor implements HandshakeInterceptor {

    public static final String HTTP_SESSION_ID_ATTR = "MA_HTTP_SESSION_ID";
    public static final String USER_ATTR = "MA_USER";
    public static final String AUTHENTICATION_ATTR = "MA_AUTHENTICATION";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        HttpSession session = getSession(request);
        if (session != null) {
            attributes.put(HTTP_SESSION_ID_ATTR, session.getId());
        }

        // get the user at the time of HTTP -> websocket upgrade
        Principal principal = request.getPrincipal();
        if (principal instanceof Authentication) {
            Authentication authentication = (Authentication) principal;
            attributes.put(AUTHENTICATION_ATTR, authentication);

            Object authenticationPrincipal = authentication.getPrincipal();
            if (authenticationPrincipal instanceof PermissionHolder) {
                attributes.put(USER_ATTR, authenticationPrincipal);
            }
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception exception) {

    }

    private HttpSession getSession(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest serverRequest = (ServletServerHttpRequest) request;
            return serverRequest.getServletRequest().getSession(false);
        }
        return null;
    }

}
