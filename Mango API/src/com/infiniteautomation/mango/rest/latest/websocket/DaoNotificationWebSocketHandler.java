/**
 * Copyright (C) 2015 Infinite Automation Systems. All rights reserved.
 * http://infiniteautomation.com/
 */
package com.infiniteautomation.mango.rest.latest.websocket;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.events.DaoEventType;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.AbstractBasicVO;
import com.serotonin.m2m2.vo.AbstractVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Jared Wiltshire
 */
public abstract class DaoNotificationWebSocketHandler<T extends AbstractBasicVO> extends MultiSessionWebSocketHandler {

    /**
     * @param action add, update or delete
     * @param vo
     * @param originalVo
     */
    public void notify(String action, T vo, T originalVo) {
        for (WebSocketSession session : sessions) {
            PermissionHolder user = getUser(session);
            if (hasPermission(user, vo) && isSubscribed(session, action, vo, originalVo)) {
                this.permissionService.runAs(user, () -> {
                    Object userMessage = createNotification(action, vo, originalVo, user);
                    if (userMessage != null) {
                        try {
                            ObjectWriter writer;
                            Class<?> view = this.viewForUser(user);
                            if (view != null) {
                                writer = this.jacksonMapper.writerWithView(view);
                            } else {
                                writer = this.jacksonMapper.writer();
                            }

                            String userJsonMessage = writer.writeValueAsString(userMessage);
                            notify(session, userJsonMessage);
                        } catch (JsonProcessingException e) {
                            log.warn("Failed to write object as JSON", e);
                        }
                    }
                });
            }
        }
    }

    abstract protected boolean hasPermission(PermissionHolder user, T vo);

    abstract protected Object createModel(T vo, PermissionHolder user);

    protected Class<?> defaultView() {
        return null;
    }

    protected Class<?> viewForUser(PermissionHolder user) {
        return null;
    }

    protected boolean isSubscribed(WebSocketSession session, String action, T vo, T originalVo) {
        return true;
    }

    /**
     * You must annotate the overridden method with @EventListener in order for this to work
     *
     * @param event
     */
    abstract protected void handleDaoEvent(DaoEvent<? extends T> event);

    protected void notify(DaoEvent<? extends T> event) {
        DaoEventType type = event.getType();
        String action = null;
        switch (type) {
            case CREATE:
                action = "create";
                break;
            case DELETE:
                action = "delete";
                break;
            case UPDATE:
                action = "update";
                break;
        }
        this.notify(action, event.getVo(), event.getOriginalVo());
    }

    protected void notify(WebSocketSession session, String jsonMessage) {
        try {
            this.sendStringMessageAsync(session, jsonMessage);
        } catch (WebSocketSendException e) {
            log.warn("Error notifying websocket", e);
        } catch (Exception e) {
            try {
                this.sendErrorMessage(session, MangoWebSocketErrorType.SERVER_ERROR, new TranslatableMessage("rest.error.serverError", e.getMessage()));
            } catch (Exception e1) {
                log.error(e1.getMessage(), e1);
            }
        }
    }

    protected Object createNotification(String action, T vo, T originalVo, PermissionHolder user) {
        Integer id = (vo instanceof AbstractBasicVO) ? ((AbstractBasicVO)vo).getId() : null;
        String xid = (vo instanceof AbstractVO) ? ((AbstractVO)vo).getXid() : null;
        String originalXid = (originalVo instanceof AbstractVO) ? ((AbstractVO)originalVo).getXid() : null;

        DaoNotificationModel payload;
        if(StringUtils.equals(action, "delete")) {
            payload = new DaoNotificationModel(action, id, xid, null, originalXid);
        }else {
            Object model = createModel(vo, user);
            if (model == null) {
                return null;
            }
            payload = new DaoNotificationModel("create".equals(action) ? "add" : action, id, xid, model, originalXid);
        }
        return new MangoWebSocketResponseModel(MangoWebSocketResponseStatus.OK, payload);
    }
}
