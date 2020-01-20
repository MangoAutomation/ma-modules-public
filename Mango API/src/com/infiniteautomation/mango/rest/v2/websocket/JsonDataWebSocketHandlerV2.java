/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.websocket;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.jsondata.JsonDataModel;
import com.infiniteautomation.mango.rest.v2.websocket.dao.SubscriptionDaoWebSocketHandler;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.serotonin.m2m2.vo.json.JsonDataVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;


/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/json-data")
public class JsonDataWebSocketHandlerV2 extends SubscriptionDaoWebSocketHandler<JsonDataVO> {

    private final PermissionService service;
    public JsonDataWebSocketHandlerV2(PermissionService service) {
        this.service = service;
    }

    @Override
    protected boolean hasPermission(PermissionHolder user, JsonDataVO vo) {
        return service.hasAnyRole(user, vo.getReadRoles());
    }

    @Override
    protected Object createModel(JsonDataVO vo, PermissionHolder user) {
        return new JsonDataModel(vo);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends JsonDataVO> event) {
        this.notify(event);
    }

}
