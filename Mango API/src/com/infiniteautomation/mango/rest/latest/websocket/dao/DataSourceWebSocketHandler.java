/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.datasource.AbstractDataSourceModel;
import com.infiniteautomation.mango.rest.latest.websocket.DaoNotificationWebSocketHandler;
import com.infiniteautomation.mango.rest.latest.websocket.WebSocketMapping;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.events.StateChangeEvent;
import com.infiniteautomation.mango.spring.service.DataSourceService;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component("DataSourceWebSocketHandlerV2")
@WebSocketMapping("/websocket/data-sources")
public class DataSourceWebSocketHandler<T extends DataSourceVO> extends DaoNotificationWebSocketHandler<T>{

    private final DataSourceService service;
    private final RestModelMapper modelMapper;

    @Autowired
    public DataSourceWebSocketHandler(DataSourceService service, RestModelMapper modelMapper) {
        this.service = service;
        this.modelMapper = modelMapper;
    }

    @Override
    protected boolean hasPermission(PermissionHolder user, T vo) {
        return service.hasReadPermission(user, vo);
    }

    @Override
    protected Object createModel(T vo, ApplicationEvent event, PermissionHolder user) {
        AbstractDataSourceModel model = modelMapper.map(vo, AbstractDataSourceModel.class, user);

        if(event instanceof StateChangeEvent) {
            //Override the state set by the mapping in case it had already changed
            //  by the time it was set
            model.setLifecycleState(((StateChangeEvent) event).getState());
        }

        return model;
    }

    @EventListener
    private void handleStateChangeEvent(StateChangeEvent<T> event) {
        this.notify(StateChangeEvent.STATE_CHANGE, event.getVo(), null, event);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends T> event) {
        this.notify(event);
    }

}
