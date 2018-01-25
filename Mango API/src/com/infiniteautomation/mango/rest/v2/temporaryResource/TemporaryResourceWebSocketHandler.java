/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.temporaryResource;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResource.TemporaryResourceStatus;
import com.infiniteautomation.mango.rest.v2.util.CrudNotificationType;
import com.infiniteautomation.mango.rest.v2.util.MangoV2WebSocketHandler;
import com.serotonin.m2m2.vo.User;

/**
 * @author Jared Wiltshire
 */
public class TemporaryResourceWebSocketHandler extends MangoV2WebSocketHandler {
    private static final String SUBSCRIPTION_ATTRIBUTE = "TemporaryResourceSubscription";
    public static final String MESSAGE_TYPE_SUBSCRIPTION = "SUBSCRIPTION";

    private final Set<WebSocketSession> sessions = new HashSet<WebSocketSession>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public TemporaryResourceWebSocketHandler() {
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        
        if (log.isDebugEnabled()) {
            log.debug("Connection established for WebSocketSession " + session.getId());
        }

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

        if (log.isDebugEnabled()) {
            log.debug("Connection closed for WebSocketSession " + session.getId() + ", status " + status);
        }
        
        lock.writeLock().lock();
        try {
            sessions.remove(session);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        TemporaryResourceRequest request = this.jacksonMapper.readValue(message.getPayload(), TemporaryResourceRequest.class);
        if (request instanceof TemporaryResourceSubscription) {
            TemporaryResourceSubscription subscription = (TemporaryResourceSubscription) request;
            if (log.isDebugEnabled()) {
                log.debug("Subscription for " + session.getId() + " has been set to " + subscription);
            }
            session.getAttributes().put(SUBSCRIPTION_ATTRIBUTE, subscription);
            this.sendMessage(session, new WebSocketResponse<Void>(subscription.getSequenceNumber()));
        }
    }
    
    public void notify(CrudNotificationType type, TemporaryResource<?, ?> resource) {
        lock.readLock().lock();
        try {
            for (WebSocketSession session : sessions) {
                try {
                    notifySession(session, type, resource);
                } catch (IOException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Couldn't notify session " + session.getId() + " of change to temporary resource " + resource, e);
                    }
                }
            }
        } finally {
            lock.readLock().unlock();
        }
    }
    
    private void notifySession(WebSocketSession session, CrudNotificationType type, TemporaryResource<?, ?> resource) throws JsonProcessingException, IOException {
        User user = this.getUser(session);
        if (user == null) return;
        
        TemporaryResourceSubscription subscription = (TemporaryResourceSubscription) session.getAttributes().get(SUBSCRIPTION_ATTRIBUTE);

        if (resource.getUserId() == user.getId() || (user.isAdmin() && !subscription.isOwnResourcesOnly())) {
            Set<TemporaryResourceStatus> statuses = subscription.getStatuses();
            Set<String> resourceTypes = subscription.getResourceTypes();
            
            if ((subscription.isAnyStatus() || statuses.contains(resource.getStatus())) && (subscription.isAnyResourceType() || resourceTypes.contains(resource.getResourceType()))) {
                WebSocketNotification<TemporaryResource<?, ?>> notificationMessage = new WebSocketNotification<>(type, resource);
                boolean showResult = subscription.isShowIncompleteResult() || resource.isComplete();
                Class<?> view = showResult ? TemporaryResource.ShowResultView.class : Object.class;
                this.sendMessageUsingView(session, notificationMessage, view);
            }
        }
    }

    @JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="messageType")
    @JsonSubTypes({
        @JsonSubTypes.Type(name = MESSAGE_TYPE_SUBSCRIPTION, value = TemporaryResourceSubscription.class)
    })
    public static class TemporaryResourceRequest extends WebSocketRequest {
    }
    
    public static class TemporaryResourceSubscription extends TemporaryResourceRequest {
        private boolean ownResourcesOnly = true;
        private boolean showIncompleteResult = false;
        private boolean anyStatus = false;
        private boolean anyResourceType = false;
        private Set<TemporaryResourceStatus> statuses;
        private Set<String> resourceTypes;
        
        public boolean isOwnResourcesOnly() {
            return ownResourcesOnly;
        }

        public void setOwnResourcesOnly(boolean ownResourcesOnly) {
            this.ownResourcesOnly = ownResourcesOnly;
        }

        public Set<TemporaryResourceStatus> getStatuses() {
            return statuses == null ? Collections.emptySet() : statuses;
        }

        public void setStatuses(Set<TemporaryResourceStatus> statuses) {
            this.statuses = statuses;
        }

        public boolean isShowIncompleteResult() {
            return showIncompleteResult;
        }

        public void setShowIncompleteResult(boolean showIncompleteResult) {
            this.showIncompleteResult = showIncompleteResult;
        }

        public Set<String> getResourceTypes() {
            return resourceTypes == null ? Collections.emptySet() : resourceTypes;
        }

        public void setResourceTypes(Set<String> resourceTypes) {
            this.resourceTypes = resourceTypes;
        }

        public boolean isAnyStatus() {
            return anyStatus;
        }

        public void setAnyStatus(boolean anyStatus) {
            this.anyStatus = anyStatus;
        }

        public boolean isAnyResourceType() {
            return anyResourceType;
        }

        public void setAnyResourceType(boolean anyResourceType) {
            this.anyResourceType = anyResourceType;
        }

        @Override
        public String toString() {
            return "TemporaryResourceSubscription [ownResourcesOnly=" + ownResourcesOnly
                    + ", showIncompleteResult=" + showIncompleteResult + ", anyStatus=" + anyStatus
                    + ", anyResourceType=" + anyResourceType + ", statuses=" + statuses
                    + ", resourceTypes=" + resourceTypes + "]";
        }
    }
}
