/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.publisher.datapoint;

import org.springframework.stereotype.Component;

import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.model.DataPointModel;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 * @author Terry Packer
 *
 */
@Component("dataPointWebSocketHandler")
public class DataPointWebSocketHandler extends DaoNotificationWebSocketHandler<DataPointVO>{

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler#hasPermission(com.serotonin.m2m2.vo.User, java.lang.Object)
	 */
	@Override
	protected boolean hasPermission(User user, DataPointVO vo) {
		if(user.hasAdminPermission())
			return true;
		else 
			return Permissions.hasDataSourcePermission(user, vo.getDataSourceId());
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler#createModel(java.lang.Object)
	 */
	@Override
	protected Object createModel(DataPointVO vo) {
		return new DataPointModel(vo);
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler#getDaoBeanName()
	 */
	@Override
	public String getDaoBeanName() {
	    return "dataPointDao";
	}
}
