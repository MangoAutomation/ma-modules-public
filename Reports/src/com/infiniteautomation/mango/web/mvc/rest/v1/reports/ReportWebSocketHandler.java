/**
 * Copyright (C) 2015 Infinite Automation Systems. All rights reserved.
 * http://infiniteautomation.com/
 */
package com.infiniteautomation.mango.web.mvc.rest.v1.reports;

import com.serotonin.m2m2.reports.vo.ReportModel;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.m2m2.reports.web.ReportCommon;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 * @author Terry Packer
 */
public class ReportWebSocketHandler extends DaoNotificationWebSocketHandler<ReportVO> {
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler#hasPermission(com.serotonin.m2m2.vo.User, com.serotonin.m2m2.vo.AbstractVO)
     */
    @Override
    protected boolean hasPermission(User user, ReportVO vo) {
        try{
        	ReportCommon.ensureReportPermission(user, vo);
        	return true;
        }catch(Exception e){
        	return false;
        }
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler#createModel(com.serotonin.m2m2.vo.AbstractVO)
     */
    @Override
    protected Object createModel(ReportVO vo) {
        return new ReportModel(vo);
    }
}
