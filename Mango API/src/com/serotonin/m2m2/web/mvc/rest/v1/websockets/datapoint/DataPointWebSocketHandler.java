/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.websockets.datapoint;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.events.DataPointTagsUpdatedEvent;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.model.DataPointModel;
import com.serotonin.m2m2.web.mvc.spring.WebSocketMapping;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/data-points")
public class DataPointWebSocketHandler extends DaoNotificationWebSocketHandler<DataPointVO> {

    public static final String TAGS_UPDATED = "tagsUpdated";

    @Override
    protected boolean hasPermission(User user, DataPointVO vo) {
        return user.hasAdminPermission() || Permissions.hasDataSourcePermission(user, vo.getDataSourceId());
    }

    @Override
    protected Object createModel(DataPointVO vo) {
        return new DataPointModel(vo);
    }

    @EventListener
    private void handleDataPointTagsUpdatedEvent(DataPointTagsUpdatedEvent event) {
        this.notify(TAGS_UPDATED, event.getVo(), null, null);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends DataPointVO> event) {
        this.notify(event);
    }
}
