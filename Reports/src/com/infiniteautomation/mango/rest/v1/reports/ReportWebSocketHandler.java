/**
 * Copyright (C) 2015 Infinite Automation Systems. All rights reserved.
 * http://infiniteautomation.com/
 */
package com.infiniteautomation.mango.rest.v1.reports;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.serotonin.m2m2.reports.vo.ReportModel;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.m2m2.reports.web.ReportCommon;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.spring.WebSocketMapping;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 * @author Terry Packer
 */
@Component
@WebSocketMapping("/websocket/reports")
public class ReportWebSocketHandler extends DaoNotificationWebSocketHandler<ReportVO> {

    @Override
    protected boolean hasPermission(User user, ReportVO vo) {
        try{
            ReportCommon.ensureReportPermission(user, vo);
            return true;
        }catch(Exception e){
            return false;
        }
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
