/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.publisher.AbstractPublishedPointModel;
import com.infiniteautomation.mango.rest.latest.model.publisher.AbstractPublisherModel;
import com.infiniteautomation.mango.rest.latest.websocket.DaoNotificationWebSocketHandler;
import com.infiniteautomation.mango.rest.latest.websocket.WebSocketMapping;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.service.PublishedPointService;
import com.infiniteautomation.mango.spring.service.PublisherService;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.publish.PublishedPointVO;
import com.serotonin.m2m2.vo.publish.PublisherVO;

/**
 * @author Terry Packer
 *
 */
@Component("PublisherWebSocketHandlerV2")
@WebSocketMapping("/websocket/publishers")
public class PublisherWebSocketHandler extends DaoNotificationWebSocketHandler<PublisherVO>{

    private final PublisherService service;
    private final PublishedPointService publishedPointService;
    private final BiFunction<PublisherVO, PermissionHolder, AbstractPublisherModel<?,?>> map;

    @Autowired
    public PublisherWebSocketHandler(PublisherService service, PublishedPointService publishedPointService,
                                     RestModelMapper modelMapper) {
        this.service = service;
        this.publishedPointService = publishedPointService;
        this.map = (vo, user) -> {
            AbstractPublisherModel model = modelMapper.map(vo, AbstractPublisherModel.class, user);
            List<AbstractPublishedPointModel> points = new ArrayList<>();
            model.setPoints(points);
            for(PublishedPointVO point : publishedPointService.getPublishedPoints(vo.getId())) {
                points.add(modelMapper.map(point, AbstractPublishedPointModel.class, user));
            }
            return model;
        };
    }

    @Override
    protected boolean hasPermission(PermissionHolder user, PublisherVO vo) {
        return service.hasReadPermission(user, vo);
    }

    @Override
    protected Object createModel(PublisherVO vo, ApplicationEvent event, PermissionHolder user) {
        return map.apply(vo, user);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends PublisherVO> event) {
        this.notify(event);
    }
}
