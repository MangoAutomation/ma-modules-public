/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.event.detectors.AbstractEventDetectorModel;
import com.infiniteautomation.mango.rest.latest.websocket.DaoNotificationWebSocketHandler;
import com.infiniteautomation.mango.rest.latest.websocket.WebSocketMapping;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.service.EventDetectorsService;
import com.serotonin.m2m2.vo.event.detector.AbstractEventDetectorVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component()
@WebSocketMapping("/websocket/full-event-detectors")
public class EventDetectorWebSocketHandler <T extends AbstractEventDetectorVO> extends DaoNotificationWebSocketHandler<T> {

    private final EventDetectorsService service;
    private final RestModelMapper modelMapper;

    @Autowired
    public EventDetectorWebSocketHandler(EventDetectorsService service, RestModelMapper modelMapper) {
        this.service = service;
        this.modelMapper = modelMapper;
    }

    @Override
    protected boolean hasPermission(PermissionHolder user, T vo) {
        return service.hasReadPermission(user, vo);
    }

    @Override
    protected Object createModel(T vo, PermissionHolder user) {
        return modelMapper.map(vo, AbstractEventDetectorModel.class, user);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends T> event) {
        this.notify(event);
    }

}
