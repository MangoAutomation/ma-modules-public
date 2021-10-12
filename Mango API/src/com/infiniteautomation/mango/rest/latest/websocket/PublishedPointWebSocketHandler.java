/*
 * Copyright (C) 2021 RadixIot LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.publisher.AbstractPublishedPointModel;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.publish.PublishedPointVO;

/**
 * Published points websocket, superadmin only
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/published-points")
public class PublishedPointWebSocketHandler extends DaoNotificationWebSocketHandler<PublishedPointVO> {

    private final RestModelMapper mapper;
    private final PermissionService permissionService;

    @Autowired
    public PublishedPointWebSocketHandler(RestModelMapper mapper, PermissionService permissionService) {
        this.mapper = mapper;
        this.permissionService = permissionService;
    }

    @Override
    protected boolean hasPermission(PermissionHolder user, PublishedPointVO vo) {
        return permissionService.hasAdminRole(user);
    }

    @Override
    protected Object createModel(PublishedPointVO vo, ApplicationEvent event, PermissionHolder user) {
        AbstractPublishedPointModel model = mapper.map(vo, AbstractPublishedPointModel.class, user);
        return model;
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends PublishedPointVO> event) {
        this.notify(event);
    }
}
