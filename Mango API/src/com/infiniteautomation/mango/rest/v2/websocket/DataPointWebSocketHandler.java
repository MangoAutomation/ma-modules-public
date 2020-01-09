/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.dataPoint.DataPointModel;
import com.infiniteautomation.mango.spring.db.DataPointTableDefinition;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.events.DataPointTagsUpdatedEvent;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/data-points")
public class DataPointWebSocketHandler extends DaoNotificationWebSocketHandler<DataPointVO, DataPointTableDefinition> {

    public static final String TAGS_UPDATED = "tagsUpdated";

    final RestModelMapper mapper;
    private final PermissionService permissionService;

    @Autowired
    public DataPointWebSocketHandler(RestModelMapper mapper, PermissionService permissionService) {
        this.mapper = mapper;
        this.permissionService = permissionService;
    }

    @Override
    protected boolean hasPermission(User user, DataPointVO vo) {
        return user.hasAdminRole() || permissionService.hasDataSourcePermission(user, vo.getDataSourceId());
    }

    @Override
    protected Object createModel(DataPointVO vo, User user) {
        return mapper.map(vo, DataPointModel.class, user);
    }

    @EventListener
    private void handleDataPointTagsUpdatedEvent(DataPointTagsUpdatedEvent event) {
        this.notify(TAGS_UPDATED, event.getVo(), null);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends DataPointVO, DataPointTableDefinition> event) {
        this.notify(event);
    }

    @Override
    protected Object createModel(DataPointVO vo) {
        throw new ShouldNeverHappenException("Should have user available.");
    }
}
