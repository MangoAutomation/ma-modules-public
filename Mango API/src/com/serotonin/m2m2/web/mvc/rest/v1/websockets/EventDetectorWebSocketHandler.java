/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.websockets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.detector.AbstractEventDetectorVO;
import com.serotonin.m2m2.vo.event.detector.AbstractPointEventDetectorVO;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.spring.WebSocketMapping;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/event-detectors")
public class EventDetectorWebSocketHandler extends DaoNotificationWebSocketHandler<AbstractEventDetectorVO<?>>{

    private final DataPointDao dataPointDao;

    @Autowired
    public EventDetectorWebSocketHandler(DataPointDao dataPointDao) {
        this.dataPointDao = dataPointDao;
    }

    @Override
    protected boolean hasPermission(User user, AbstractEventDetectorVO<?> vo) {
        if (user.hasAdminPermission()) {
            return true;
        }

        if (vo instanceof AbstractPointEventDetectorVO) {
            AbstractPointEventDetectorVO<?> ped = (AbstractPointEventDetectorVO<?>) vo;
            DataPointVO point = ped.njbGetDataPoint();
            if (point == null) {
                point = dataPointDao.get(vo.getSourceId());
            }
            if (point != null) {
                return Permissions.hasDataSourcePermission(user, point.getDataSourceId());
            }
        }

        return false;
    }

    @Override
    protected Object createModel(AbstractEventDetectorVO<?> vo) {
        return vo.asModel();
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends AbstractEventDetectorVO<?>> event) {
        this.notify(event);
    }

}
