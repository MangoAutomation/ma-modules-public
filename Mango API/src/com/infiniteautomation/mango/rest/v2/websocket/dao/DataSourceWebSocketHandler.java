/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.websocket.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.event.handlers.AbstractEventHandlerModel;
import com.infiniteautomation.mango.rest.v2.websocket.DaoNotificationWebSocketHandler;
import com.infiniteautomation.mango.rest.v2.websocket.WebSocketMapping;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.service.DataSourceService;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

/**
 * @author Terry Packer
 *
 */
@Component("DataSourceWebSocketHandlerV2")
@WebSocketMapping("/websocket/data-sources")
public class DataSourceWebSocketHandler extends DaoNotificationWebSocketHandler<DataSourceVO<?>>{

    private final DataSourceService service;
    private final RestModelMapper modelMapper;

    @Autowired
    public DataSourceWebSocketHandler(DataSourceService service, RestModelMapper modelMapper) {
        this.service = service;
        this.modelMapper = modelMapper;
    }

    @Override
    protected boolean hasPermission(User user, DataSourceVO<?> vo) {
        return service.hasReadPermission(user, vo);
    }

    @Override
    protected Object createModel(DataSourceVO<?> vo) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object createModel(DataSourceVO<?> vo, User user) {
        return modelMapper.map(vo, AbstractEventHandlerModel.class, user);
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
