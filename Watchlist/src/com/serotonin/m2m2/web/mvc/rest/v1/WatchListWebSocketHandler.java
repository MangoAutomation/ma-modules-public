/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.watchlist.WatchListVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.WatchListDataPointModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.WatchListModel;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 * @author Terry Packer
 *
 */
@Component("watchListWebSocketHandler")
public class WatchListWebSocketHandler extends DaoNotificationWebSocketHandler<WatchListVO> {
    
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler#getDaoBeanName()
     */
    @Override
    public String getDaoBeanName() {
        return "watchListDao";
    }
    
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler#hasPermission(com.serotonin.m2m2.vo.User, com.serotonin.m2m2.vo.AbstractVO)
     */
    @Override
    protected boolean hasPermission(User user, WatchListVO vo) {
        return WatchListRestController.hasReadPermission(user, vo);
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler#createModel(com.serotonin.m2m2.vo.AbstractVO)
     */
    @Override
    protected Object createModel(WatchListVO vo) {
    	List<WatchListDataPointModel> points = new ArrayList<WatchListDataPointModel>();
    	
    	for(DataPointVO dp : vo.getPointList())
    		points.add(new WatchListDataPointModel(dp));
    	
        return new WatchListModel(vo, points);
    }
}