/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.temporaryResource;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResource.TemporaryResourceStatus;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketErrorType;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketHandler;

/**
 * @author Jared Wiltshire
 */
public class TemporaryResourceWebSocketHandler extends MangoWebSocketHandler {
    
    public static class TemporaryResourceSubscription {
        private boolean ownResourcesOnly = true;
        private Set<TemporaryResourceStatus> statuses = new HashSet<>();
        
        public boolean isOwnResourcesOnly() {
            return ownResourcesOnly;
        }

        public void setOwnResourcesOnly(boolean ownResourcesOnly) {
            this.ownResourcesOnly = ownResourcesOnly;
        }

        public Set<TemporaryResourceStatus> getStatuses() {
            return statuses;
        }

        public void setStatuses(Set<TemporaryResourceStatus> statuses) {
            this.statuses = statuses;
        }
    }
    
    private static final String SUBSCRIPTION_ATTRIBUTE = "TemporaryResourceSubscription";

    private final Log log = LogFactory.getLog(this.getClass());
    private final Set<WebSocketSession> sessions = new HashSet<WebSocketSession>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    public TemporaryResourceWebSocketHandler() {
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);

        session.getAttributes().put(SUBSCRIPTION_ATTRIBUTE, new TemporaryResourceSubscription());
        
        lock.writeLock().lock();
        try {
            sessions.add(session);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        lock.writeLock().lock();
        try {
            sessions.remove(session);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        TemporaryResourceSubscription subscription = this.jacksonMapper.readValue(message.getPayload(), TemporaryResourceSubscription.class);
        session.getAttributes().put(SUBSCRIPTION_ATTRIBUTE, subscription);
    }
    
    public void notify(TemporaryResource<?, ?> resource) {
        lock.readLock().lock();
        try {
            for (WebSocketSession session : sessions)
                notifySession(session, resource);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void notifySession(WebSocketSession session, TemporaryResource<?, ?> resource) {
        User user = this.getUser(session);
        if (user == null) return;
        
        TemporaryResourceSubscription subscription = (TemporaryResourceSubscription) session.getAttributes().get(SUBSCRIPTION_ATTRIBUTE);
        Set<TemporaryResourceStatus> statuses = subscription.getStatuses();
        
        if (resource.getUserId() == user.getId() || (user.isAdmin() && !subscription.isOwnResourcesOnly())) {
            if (statuses.contains(resource.getStatus())) {
                try {
                    this.sendMessage(session, resource);
                } catch (Exception e) {
                    try {
                        this.sendErrorMessage(session, MangoWebSocketErrorType.SERVER_ERROR, new TranslatableMessage("rest.error.serverError", e.getMessage()));
                    } catch (Exception e1) {
                        if (log.isErrorEnabled()) {
                            log.error("Error sending temporary resource websocket notification", e1);
                        }
                    }
                }
            }
        }
    }
}
