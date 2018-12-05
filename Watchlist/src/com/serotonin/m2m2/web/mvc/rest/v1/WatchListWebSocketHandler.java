/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.watchlist.WatchListVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.WatchListDataPointModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.WatchListModel;
import com.serotonin.m2m2.web.mvc.spring.WebSocketMapping;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/watch-lists")
public class WatchListWebSocketHandler extends DaoNotificationWebSocketHandler<WatchListVO> {

    @Override
    protected boolean hasPermission(User user, WatchListVO vo) {
        return WatchListRestController.hasReadPermission(user, vo);
    }

    @Override
    protected Object createModel(WatchListVO vo) {
        List<WatchListDataPointModel> points = new ArrayList<WatchListDataPointModel>();

        for(DataPointVO dp : vo.getPointList())
            points.add(new WatchListDataPointModel(dp));

        return new WatchListModel(vo, points);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends WatchListVO> event) {
        this.notify(event);
    }
}
