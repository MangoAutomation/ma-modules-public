/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.dataPoint.DataPointModel;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.events.DataPointTagsUpdatedEvent;
import com.infiniteautomation.mango.spring.events.StateChangeEvent;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/data-points")
public class DataPointWebSocketHandler extends DaoNotificationWebSocketHandler<DataPointVO> {

    public static final String TAGS_UPDATED = "tagsUpdated";

    final RestModelMapper mapper;
    private final PermissionService permissionService;

    @Autowired
    public DataPointWebSocketHandler(RestModelMapper mapper, PermissionService permissionService) {
        this.mapper = mapper;
        this.permissionService = permissionService;
    }

    @Override
    protected boolean hasPermission(PermissionHolder user, DataPointVO vo) {
        return permissionService.hasAdminRole(user) || permissionService.hasPermission(user, vo.getEditPermission());
    }

    @Override
    protected Object createModel(DataPointVO vo, ApplicationEvent event, PermissionHolder user) {
        DataPointModel model = mapper.map(vo, DataPointModel.class, user);
        if(event instanceof StateChangeEvent) {
            //Override the state set by the mapping in case it had already changed
            //  by the time it was set
            model.setLifecycleState(((StateChangeEvent) event).getState());
        }
        return model;
    }

    @EventListener
    private void handleStateChangeEvent(StateChangeEvent<DataPointVO> event) {
        this.notify(StateChangeEvent.STATE_CHANGE, event.getVo(), null, event);
    }

    @EventListener
    private void handleDataPointTagsUpdatedEvent(DataPointTagsUpdatedEvent event) {
        this.notify(TAGS_UPDATED, event.getVo(), null, event);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends DataPointVO> event) {
        this.notify(event);
    }

}
