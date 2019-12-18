/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.reports;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.websocket.DaoNotificationWebSocketHandler;
import com.infiniteautomation.mango.rest.v2.websocket.WebSocketMapping;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.service.reports.ReportsService;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.m2m2.vo.User;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/reports")
public class ReportWebSocketHandler extends DaoNotificationWebSocketHandler<ReportVO> {

    private final ReportsService service;
    
    @Autowired
    public ReportWebSocketHandler(ReportsService service) {
        this.service = service;
    }
    
    @Override
    protected boolean hasPermission(User user, ReportVO vo) {
        return service.hasReadPermission(user, vo);
    }

    @Override
    protected Object createModel(ReportVO vo) {
        return new ReportModel(vo);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends ReportVO> event) {
        this.notify(event);
    }

}
