/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.latest.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.dataPoint.DataPointModel;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.events.DataPointTagsUpdatedEvent;
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
        return permissionService.hasAdminRole(user) || permissionService.hasDataSourceEditPermission(user, vo.getDataSourceId());
    }

    @Override
    protected Object createModel(DataPointVO vo, PermissionHolder user) {
        return mapper.map(vo, DataPointModel.class, user);
    }

    @EventListener
    private void handleDataPointTagsUpdatedEvent(DataPointTagsUpdatedEvent event) {
        this.notify(TAGS_UPDATED, event.getVo(), null);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends DataPointVO> event) {
        this.notify(event);
    }
}
