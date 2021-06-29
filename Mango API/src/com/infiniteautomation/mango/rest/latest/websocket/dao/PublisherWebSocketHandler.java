/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.publisher.AbstractPublisherModel;
import com.infiniteautomation.mango.rest.latest.websocket.DaoNotificationWebSocketHandler;
import com.infiniteautomation.mango.rest.latest.websocket.WebSocketMapping;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.service.PublisherService;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.publish.PublishedPointVO;
import com.serotonin.m2m2.vo.publish.PublisherVO;

/**
 * @author Terry Packer
 *
 */
@Component("PublisherWebSocketHandlerV2")
@WebSocketMapping("/websocket/publishers")
public class PublisherWebSocketHandler extends DaoNotificationWebSocketHandler<PublisherVO<? extends PublishedPointVO>>{

    private final PublisherService service;
    private final RestModelMapper modelMapper;

    @Autowired
    public PublisherWebSocketHandler(PublisherService service, RestModelMapper modelMapper) {
        this.service = service;
        this.modelMapper = modelMapper;
    }

    @Override
    protected boolean hasPermission(PermissionHolder user, PublisherVO<? extends PublishedPointVO> vo) {
        return service.hasReadPermission(user, vo);
    }

    @Override
    protected Object createModel(PublisherVO<? extends PublishedPointVO> vo, ApplicationEvent event, PermissionHolder user) {
        return modelMapper.map(vo, AbstractPublisherModel.class, user);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends PublisherVO<? extends PublishedPointVO>> event) {
        this.notify(event);
    }
}
