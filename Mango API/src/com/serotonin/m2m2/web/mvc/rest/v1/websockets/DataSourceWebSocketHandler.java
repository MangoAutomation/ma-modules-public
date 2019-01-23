/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.websockets;

import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.WebSocketMapping;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelMapper;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.AbstractDataSourceModel;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/data-sources")
public class DataSourceWebSocketHandler extends DaoNotificationWebSocketHandler<DataSourceVO<?>> {

    private final BiFunction<DataSourceVO<?>, User, AbstractDataSourceModel<?>> map;

    @Autowired
    public DataSourceWebSocketHandler(final RestModelMapper modelMapper) {
        this.map = (vo, user) -> {
            return modelMapper.map(vo, AbstractDataSourceModel.class, user);
        };
    }
    
    @Override
    protected boolean hasPermission(User user, DataSourceVO<?> vo) {
        if(user.hasAdminPermission())
            return true;
        else
            return Permissions.hasDataSourcePermission(user, vo);
    }

    @Override
    protected Object createModel(DataSourceVO<?> vo) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected Object createModel(DataSourceVO<?> vo, User user) {
        return map.apply(vo, user);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends DataSourceVO<?>> event) {
        this.notify(event);
    }

    @Override
    protected boolean isModelPerUser() {
        return true;
    }
}
