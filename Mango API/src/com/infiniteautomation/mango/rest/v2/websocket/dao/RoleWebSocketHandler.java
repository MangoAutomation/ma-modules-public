/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.websocket.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.role.RoleModelMapping;
import com.infiniteautomation.mango.rest.v2.websocket.WebSocketMapping;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.service.RoleService;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.role.RoleVO;

/**
 * @author Terry Packer
 */
@Component
@WebSocketMapping("/websocket/roles")
public class RoleWebSocketHandler extends SubscriptionDaoWebSocketHandler<RoleVO> {

    private final RoleService service;
    private final RoleModelMapping mapping;
    private final RestModelMapper mapper;

    @Autowired
    public RoleWebSocketHandler(RoleService service, RoleModelMapping mapping,
            RestModelMapper mapper) {
        this.service = service;
        this.mapping = mapping;
        this.mapper = mapper;
    }

    @Override
    protected boolean hasPermission(User user, RoleVO vo) {
        return service.hasReadPermission(user, vo);
    }

    @Override
    protected Object createModel(RoleVO vo, User user) {
        return mapping.map(vo, user, mapper);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends RoleVO> event) {
        this.notify(event);
    }
}
