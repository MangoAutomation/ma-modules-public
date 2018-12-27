/**
 * Copyright (C) 2015 Infinite Automation Systems. All rights reserved.
 * http://infiniteautomation.com/
 */
package com.infiniteautomation.mango.rest.v2.websocket;

import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.events.DaoEventType;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.AbstractBasicVO;
import com.serotonin.m2m2.vo.User;

/**
 * @author Jared Wiltshire
 */
public abstract class DaoNotificationWebSocketHandler<T extends AbstractBasicVO> extends MultiSessionWebSocketHandler {

    /**
     * @param action add, update or delete
     * @param vo
     * @param initiatorId random string to identify who initiated the event
     */
    public void notify(String action, T vo, String initiatorId, String originalXid) {
        if (sessions.isEmpty()) return;

        Object message = null;
        String jsonMessage = null;

        if (!this.isModelPerUser()) {
            message = createNotification(action, vo, initiatorId, originalXid, null);

            if (!this.isViewPerUser()) {
                ObjectWriter writer;
                Class<?> view = this.defaultView();
                if (view != null) {
                    writer = this.jacksonMapper.writerWithView(view);
                } else {
                    writer = this.jacksonMapper.writer();
                }

                try {
                    jsonMessage = writer.writeValueAsString(message);
                } catch (JsonProcessingException e) {
                    log.warn("Failed to write object as JSON", e);
                    return;
                }
            }
        }

        for (WebSocketSession session : sessions) {
            User user = getUser(session);
            if (user != null && hasPermission(user, vo) && isSubscribed(session, action, vo, originalXid)) {
                Object userMessage = message;
                String userJsonMessage = jsonMessage;

                if (userMessage == null) {
                    userMessage = createNotification(action, vo, initiatorId, originalXid, user);
                    if (userMessage == null) {
                        continue;
                    }
                }
                if (userJsonMessage == null) {
                    try {
                        ObjectWriter writer;
                        Class<?> view = this.viewForUser(user);
                        if (view != null) {
                            writer = this.jacksonMapper.writerWithView(view);
                        } else {
                            writer = this.jacksonMapper.writer();
                        }

                        userJsonMessage = writer.writeValueAsString(userMessage);
                    } catch (JsonProcessingException e) {
                        log.warn("Failed to write object as JSON", e);
                        continue;
                    }
                }

                notify(session, userJsonMessage);
            }
        }
    }

    abstract protected boolean hasPermission(User user, T vo);
    abstract protected Object createModel(T vo);

    protected Object createModel(T vo, User user) {
        return createModel(vo);
    }

    protected boolean isModelPerUser() {
        return false;
    }

    protected boolean isViewPerUser() {
        return false;
    }

    protected Class<?> defaultView() {
        return null;
    }

    protected Class<?> viewForUser(User user) {
        return null;
    }

    protected boolean isSubscribed(WebSocketSession session, String action, T vo, String originalXid) {
        return true;
    }

    /**
     * You must annotate the overridden method with @EventListener in order for this to work
     * @param event
     */
    abstract protected void handleDaoEvent(DaoEvent<? extends T> event);

    protected void notify(DaoEvent<? extends T> event) {
        DaoEventType type = event.getType();
        String action = null;
        switch(type) {
            case CREATE: action = "create"; break;
            case DELETE: action = "delete"; break;
            case UPDATE: action = "update"; break;
        }
        this.notify(action, event.getVo(), event.getInitiatorId(), event.getOriginalXid());
    }

    protected void notify(WebSocketSession session, String jsonMessage) {
        try {
            this.sendStringMessageAsync(session, jsonMessage);
        } catch(WebSocketSendException e) {
            log.warn("Error notifying websocket", e);
        } catch (Exception e) {
            try {
                this.sendErrorMessage(session, MangoWebSocketErrorType.SERVER_ERROR, new TranslatableMessage("rest.error.serverError", e.getMessage()));
            } catch (Exception e1) {
                log.error(e1.getMessage(), e1);
            }
        }
    }

    protected Object createNotification(String action, T vo, String initiatorId, String originalXid, User user) {
        Object model = createModel(vo, user);
        if (model == null) {
            return null;
        }

        DaoNotificationModel payload = new DaoNotificationModel("create".equals(action) ? "add" : action, model, initiatorId, originalXid);
        return new MangoWebSocketResponseModel(MangoWebSocketResponseStatus.OK, payload);
    }
}
