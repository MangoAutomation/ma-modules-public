/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.infiniteautomation.mango.spring.components.RunAs;
import com.infiniteautomation.mango.spring.events.AuthTokensRevokedEvent;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.events.DaoEventType;
import com.serotonin.m2m2.util.timeout.TimeoutClient;
import com.serotonin.m2m2.util.timeout.TimeoutTask;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.web.mvc.spring.security.authentication.JwtAuthentication;

/**
 * Tracks websocket sessions by HTTP session, JWT token, user id etc and closes the sessions whenever the authentication is no longer valid.
 *
 * @author Jared Wiltshire
 */
@Service("mangoWebSocketSessionTrackerV2")
public final class MangoWebSocketSessionTracker {

    public final static CloseStatus SESSION_DESTROYED = new CloseStatus(4101, "Session destroyed");
    public final static CloseStatus USER_UPDATED = new CloseStatus(4102, "User updated (deleted/disabled/password or permissions changed)");
    public final static CloseStatus USER_AUTH_TOKENS_REVOKED = new CloseStatus(4103, "User auth tokens revoked");
    public final static CloseStatus USER_AUTH_TOKEN_EXPIRED = new CloseStatus(4104, "User auth token expired");
    public final static CloseStatus AUTH_TOKENS_REVOKED = new CloseStatus(4105, "Auth tokens revoked");

    public static final String CLOSE_TIMEOUT_TASK_ATTR = "MA_CLOSE_TIMEOUT_TASK";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Map of http session ids to a set of websocket sessions which are associated with it
     */
    private final SetMultimap<String, WebSocketSession> sessionsByHttpSessionId = Multimaps.synchronizedSetMultimap(HashMultimap.create());

    /**
     * Map of user ids to a set of websocket sessions which are associated with it (JWT authenticated sessions only)
     */
    private final SetMultimap<Integer, WebSocketSession> jwtSessionsByUserId = Multimaps.synchronizedSetMultimap(HashMultimap.create());

    /**
     * Map of user ids to a set of websocket sessions which are associated with it (session/basic authenticated sessions only)
     */
    private final SetMultimap<Integer, WebSocketSession> otherSessionsByUserId = Multimaps.synchronizedSetMultimap(HashMultimap.create());

    /**
     * Set of sessions which were established using JWT authentication
     */
    private final Set<WebSocketSession> jwtSessions = ConcurrentHashMap.newKeySet();

    @Autowired
    private RunAs runAs;

    private String httpSessionIdForSession(WebSocketSession session) {
        return (String) session.getAttributes().get(MangoWebSocketHandshakeInterceptor.HTTP_SESSION_ID_ATTR);
    }

    private PermissionHolder userForSession(WebSocketSession session) {
        return (PermissionHolder) session.getAttributes().get(MangoWebSocketHandshakeInterceptor.USER_ATTR);
    }

    private Authentication authenticationForSession(WebSocketSession session) {
        Principal principal = session.getPrincipal();
        if (principal instanceof Authentication) {
            return (Authentication) principal;
        }
        return null;
    }

    private void closeSession(WebSocketSession session, CloseStatus status) {
        try {
            if (session.isOpen()) {
                session.close(status);
            }
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("Couldn't close WebSocket session " + session.getId(), e);
            }
        }
    }

    @EventListener
    private void sessionDestroyed(SessionDestroyedEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Session destroyed, closing any websockets associated with the session " + event.getId());
        }

        String httpSessionId = event.getId();

        Set<WebSocketSession> sessions = sessionsByHttpSessionId.removeAll(httpSessionId);
        for (WebSocketSession session : sessions) {
            closeSession(session, SESSION_DESTROYED);
        }
    }

    @EventListener
    private void userDaoEvent(DaoEvent<User> event) {
        if (log.isDebugEnabled()) {
            log.debug("User DAO event received " + event.toString());
        }

        if (event.getType() == DaoEventType.DELETE) {
            userDeleted(event);
        } else if (event.getType() == DaoEventType.UPDATE) {
            userUpdated(event);
        }
    }

    private void userDeleted(DaoEvent<User> event) {
        int userId = event.getVo().getId();

        Set<WebSocketSession> jwtSessions = jwtSessionsByUserId.removeAll(userId);
        if (!jwtSessions.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Closing JWT authenticated WebSocket sessions for deleted user " + event.getVo().getUsername());
            }

            for (WebSocketSession session : jwtSessions) {
                closeSession(session, USER_UPDATED);
            }
        }

        Set<WebSocketSession> otherSessions = otherSessionsByUserId.removeAll(userId);
        if (!otherSessions.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Closing session/basic authenticated WebSocket sessions for deleted user " + event.getVo().getUsername());
            }

            for (WebSocketSession session : otherSessions) {
                closeSession(session, USER_UPDATED);
            }
        }
    }

    private void userUpdated(DaoEvent<User> event) {
        User updatedUser = event.getVo();
        int userId = updatedUser.getId();

        User old = event.getOriginalVo();
        boolean disabledOrPermissionsChanged = updatedUser.isDisabled() || !updatedUser.getRoles().equals(old.getRoles());
        boolean authTokensRevoked = updatedUser.getTokenVersion() > old.getTokenVersion();
        boolean passwordChanged = !StringUtils.equals(updatedUser.getPassword(), old.getPassword());

        if (disabledOrPermissionsChanged || authTokensRevoked) {
            Set<WebSocketSession> sessions = jwtSessionsByUserId.removeAll(userId);
            if (!sessions.isEmpty() && log.isDebugEnabled()) {
                log.debug("Closing JWT authenticated WebSocket sessions for updated user '" + updatedUser.getUsername() + "'");
            }

            for (WebSocketSession session : sessions) {
                closeSession(session, authTokensRevoked ? USER_AUTH_TOKENS_REVOKED : USER_UPDATED);
            }
        } else {
            Set<WebSocketSession> sessions = jwtSessionsByUserId.get(userId);
            synchronized (jwtSessionsByUserId) {
                if (!sessions.isEmpty() && log.isDebugEnabled()) {
                    log.debug("Storing updated user '" + updatedUser.getUsername() + "' in JWT authenticated WebSocket sessions");
                }

                for (WebSocketSession session : sessions) {
                    // store the updated user in the session attributes
                    session.getAttributes().put(MangoWebSocketHandshakeInterceptor.USER_ATTR, updatedUser);
                }
            }
        }

        if (disabledOrPermissionsChanged || passwordChanged) {
            Set<WebSocketSession> sessions = otherSessionsByUserId.removeAll(userId);
            if (!sessions.isEmpty() && log.isDebugEnabled()) {
                log.debug("Closing session/basic authenticated WebSocket sessions for updated user '" + updatedUser.getUsername() + "'");
            }

            for (WebSocketSession session : sessions) {
                closeSession(session, USER_UPDATED);
            }
        } else {
            Set<WebSocketSession> sessions = otherSessionsByUserId.get(userId);
            synchronized (otherSessionsByUserId) {
                if (!sessions.isEmpty() && log.isDebugEnabled()) {
                    log.debug("Storing updated user '" + updatedUser.getUsername() + "' in session/basic authenticated WebSocket sessions");
                }

                for (WebSocketSession session : sessions) {
                    // store the updated user in the session attributes
                    session.getAttributes().put(MangoWebSocketHandshakeInterceptor.USER_ATTR, updatedUser);
                }
            }
        }
    }

    @EventListener
    private void allAuthTokensRevoked(AuthTokensRevokedEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("All JWT authentication tokens have been revoked (key changed), closing all JWT authenticated WebSocket sessions");
        }

        Iterator<WebSocketSession> it = jwtSessions.iterator();
        while (it.hasNext()) {
            WebSocketSession session = it.next();
            closeSession(session, AUTH_TOKENS_REVOKED);
            it.remove();
        }
    }

    public void afterConnectionEstablished(WebSocketSession session) {
        String httpSessionId = this.httpSessionIdForSession(session);
        if (httpSessionId != null) {
            sessionsByHttpSessionId.put(httpSessionId, session);
        }

        PermissionHolder permissionHolder = this.userForSession(session);
        Authentication authentication = this.authenticationForSession(session);
        boolean isJwt = authentication instanceof JwtAuthentication;

        User user = permissionHolder.getUser();
        if (user != null) {
            int userId = user.getId();
            if (isJwt) {
                jwtSessionsByUserId.put(userId, session);
            } else {
                otherSessionsByUserId.put(userId, session);
            }
        }

        if (isJwt) {
            JwtAuthentication jwtAuthentication = (JwtAuthentication) authentication;
            Date expiration = jwtAuthentication.getToken().getBody().getExpiration();

            runAs.runAs(runAs.systemSuperadmin(), ()-> {
                TimeoutTask closeTask = new TimeoutTask(expiration, new CloseSessionTask(session));
                session.getAttributes().put(CLOSE_TIMEOUT_TASK_ATTR, closeTask);
            });

            jwtSessions.add(session);
        }
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        String httpSessionId = this.httpSessionIdForSession(session);
        if (httpSessionId != null) {
            sessionsByHttpSessionId.remove(httpSessionId, session);
        }

        PermissionHolder permissionHolder = this.userForSession(session);
        Authentication authentication = this.authenticationForSession(session);
        boolean isJwt = authentication instanceof JwtAuthentication;

        User user = permissionHolder.getUser();
        if (user != null) {
            int userId = user.getId();
            if (isJwt) {
                jwtSessionsByUserId.remove(userId, session);
            } else {
                otherSessionsByUserId.remove(userId, session);
            }
        }

        if (isJwt) {
            TimeoutTask closeTask = (TimeoutTask) session.getAttributes().get(CLOSE_TIMEOUT_TASK_ATTR);
            if (closeTask != null) {
                closeTask.cancel();
            }

            jwtSessions.remove(session);
        }
    }

    public class CloseSessionTask extends TimeoutClient {
        private final WebSocketSession session;

        CloseSessionTask(WebSocketSession session) {
            this.session = session;
        }

        @Override
        public void scheduleTimeout(long fireTime) {
            closeSession(session, USER_AUTH_TOKEN_EXPIRED);
        }

        @Override
        public String getThreadName() {
            return "Close websocket session task";
        }
    }

}
