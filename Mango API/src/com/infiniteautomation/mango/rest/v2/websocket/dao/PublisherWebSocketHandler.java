/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.websocket.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.publisher.AbstractPublisherModel;
import com.infiniteautomation.mango.rest.v2.websocket.DaoNotificationWebSocketHandler;
import com.infiniteautomation.mango.rest.v2.websocket.WebSocketMapping;
import com.infiniteautomation.mango.spring.db.PublisherTableDefinition;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.service.PublisherService;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.publish.PublishedPointVO;
import com.serotonin.m2m2.vo.publish.PublisherVO;

/**
 * @author Terry Packer
 *
 */
@Component("PublisherWebSocketHandlerV2")
@WebSocketMapping("/websocket/publishers")
public class PublisherWebSocketHandler <POINT extends PublishedPointVO, PUBLISHER extends PublisherVO<POINT>> extends DaoNotificationWebSocketHandler<PUBLISHER, PublisherTableDefinition>{

    private final PublisherService<POINT> service;
    private final RestModelMapper modelMapper;

    @Autowired
    public PublisherWebSocketHandler(PublisherService<POINT> service, RestModelMapper modelMapper) {
        this.service = service;
        this.modelMapper = modelMapper;
    }

    @Override
    protected boolean hasPermission(User user, PUBLISHER vo) {
        return service.hasReadPermission(user, vo);
    }

    @Override
    protected Object createModel(PUBLISHER vo, User user) {
        return modelMapper.map(vo, AbstractPublisherModel.class, user);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends PUBLISHER, PublisherTableDefinition> event) {
        this.notify(event);
    }

    @Override
    protected boolean isModelPerUser() {
        return true;
    }
}
