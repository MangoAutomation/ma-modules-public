/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.temporaryResource;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResource.TemporaryResourceStatus;
import com.infiniteautomation.mango.rest.v2.util.CrudNotificationType;
import com.infiniteautomation.mango.rest.v2.websocket.MultiSessionWebSocketHandler;
import com.infiniteautomation.mango.rest.v2.websocket.WebSocketMapping;
import com.infiniteautomation.mango.rest.v2.websocket.WebSocketNotification;
import com.infiniteautomation.mango.rest.v2.websocket.WebSocketRequest;
import com.infiniteautomation.mango.rest.v2.websocket.WebSocketResponse;
import com.serotonin.m2m2.vo.User;

/**
 * @author Jared Wiltshire
 */
@Component
@WebSocketMapping("/websocket/temporary-resources")
public class TemporaryResourceWebSocketHandler extends MultiSessionWebSocketHandler {
    private static final String SUBSCRIPTION_ATTRIBUTE = "TemporaryResourceSubscription";
    public static final String REQUEST_TYPE_SUBSCRIPTION = "SUBSCRIPTION";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);

        session.getAttributes().put(SUBSCRIPTION_ATTRIBUTE, new TemporaryResourceSubscription());
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
            this.sendRawMessage(session, new WebSocketResponse<Void>(subscription.getSequenceNumber()));
        }
    }

    public void notify(CrudNotificationType type, TemporaryResource<?, ?> resource) {
        Iterator<WebSocketSession> it = sessions.iterator();
        while (it.hasNext()) {
            WebSocketSession session = it.next();

            if (!session.isOpen()) {
                if (log.isWarnEnabled()) {
                    log.warn("Closed session " + session.getId() + " found in list of sessions to notify, removing it");
                }

                it.remove();
                continue;
            }

            try {
                notifySession(session, type, resource);
            } catch (IOException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Couldn't notify session " + session.getId() + " of change to temporary resource " + resource, e);
                }
            }
        }
    }

    private void notifySession(WebSocketSession session, CrudNotificationType type, TemporaryResource<?, ?> resource) throws JsonProcessingException, IOException {
        User user = this.getUser(session);
        if (user == null) return;

        TemporaryResourceSubscription subscription = (TemporaryResourceSubscription) session.getAttributes().get(SUBSCRIPTION_ATTRIBUTE);

        if (resource.getUserId() == user.getId() || (user.hasAdminPermission() && !subscription.isOwnResourcesOnly())) {
            Set<TemporaryResourceStatus> statuses = subscription.getStatuses();
            Set<String> resourceTypes = subscription.getResourceTypes();

            if ((subscription.isAnyStatus() || statuses.contains(resource.getStatus())) &&
                    (subscription.isAnyResourceType() || resourceTypes.contains(resource.getResourceType()))) {

                WebSocketNotification<TemporaryResource<?, ?>> notificationMessage = new WebSocketNotification<>(type, resource);
                boolean showResult = !resource.isComplete() && subscription.isShowResultWhenIncomplete() ||
                        resource.isComplete() && subscription.isShowResultWhenComplete();

                if (type == CrudNotificationType.DELETE) {
                    showResult = false;
                }

                Class<?> view = showResult ? TemporaryResourceViews.ShowResult.class : Object.class;

                if (log.isTraceEnabled()) {
                    log.trace("Notifying session " + session.getId() + " of change to resource " + resource);
                }

                try {
                    this.sendRawMessageUsingView(session, notificationMessage, view);
                } catch (Exception e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Error notifying session " + session.getId() + " of change to resource " + resource, e);
                    }
                }
            }
        }
    }

    @JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="requestType")
    @JsonSubTypes({
        @JsonSubTypes.Type(name = REQUEST_TYPE_SUBSCRIPTION, value = TemporaryResourceSubscription.class)
    })
    public static class TemporaryResourceRequest extends WebSocketRequest {
    }

    public static class TemporaryResourceSubscription extends TemporaryResourceRequest {
        private boolean ownResourcesOnly = true;
        private boolean showResultWhenIncomplete = false;
        private boolean showResultWhenComplete = false;
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

        protected boolean isShowResultWhenIncomplete() {
            return showResultWhenIncomplete;
        }

        protected void setShowResultWhenIncomplete(boolean showResultWhenIncomplete) {
            this.showResultWhenIncomplete = showResultWhenIncomplete;
        }

        protected boolean isShowResultWhenComplete() {
            return showResultWhenComplete;
        }

        protected void setShowResultWhenComplete(boolean showResultWhenComplete) {
            this.showResultWhenComplete = showResultWhenComplete;
        }

        @Override
        public String toString() {
            return "TemporaryResourceSubscription [ownResourcesOnly=" + ownResourcesOnly
                    + ", showResultWhenIncomplete=" + showResultWhenIncomplete
                    + ", showResultWhenComplete=" + showResultWhenComplete + ", anyStatus="
                    + anyStatus + ", anyResourceType=" + anyResourceType + ", statuses=" + statuses
                    + ", resourceTypes=" + resourceTypes + "]";
        }
    }
}
