/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.websocket;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.websocket.dao.SubscriptionDaoWebSocketHandler;
import com.infiniteautomation.mango.spring.db.JsonDataTableDefinition;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.json.JsonDataVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.jsondata.JsonDataModel;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/json-data")
public class JsonDataWebSocketHandlerV2 extends SubscriptionDaoWebSocketHandler<JsonDataVO, JsonDataTableDefinition> {

    private final PermissionService service;
    public JsonDataWebSocketHandlerV2(PermissionService service) {
        this.service = service;
    }

    @Override
    protected boolean hasPermission(User user, JsonDataVO vo) {
        return service.hasAnyRole(user, vo.getReadRoles());
    }

    @Override
    protected Object createModel(JsonDataVO vo) {
        return new JsonDataModel(vo);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends JsonDataVO, JsonDataTableDefinition> event) {
        this.notify(event);
    }

}
