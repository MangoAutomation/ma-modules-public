/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.role.RoleModelMapping;
import com.infiniteautomation.mango.rest.latest.websocket.WebSocketMapping;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.service.RoleService;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
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
    protected boolean hasPermission(PermissionHolder user, RoleVO vo) {
        return service.hasReadPermission(user, vo);
    }

    @Override
    protected Object createModel(RoleVO vo, ApplicationEvent event, PermissionHolder user) {
        return mapping.map(vo, user, mapper);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends RoleVO> event) {
        this.notify(event);
    }
}
