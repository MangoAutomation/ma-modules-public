/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.publisher;

import org.springframework.stereotype.Component;

import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.detector.AbstractEventDetectorVO;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 * @author Terry Packer
 *
 */
@Component("eventDetectorWebSocketHandler")
public class EventDetectorWebSocketHandler extends DaoNotificationWebSocketHandler<AbstractEventDetectorVO<?>>{

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler#hasPermission(com.serotonin.m2m2.vo.User, java.lang.Object)
	 */
	@Override
	protected boolean hasPermission(User user, AbstractEventDetectorVO<?> vo) {
		//TODO Check permissions on point or data source
		if(user.hasAdminPermission())
			return true;
		else 
			return false;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler#createModel(java.lang.Object)
	 */
	@Override
	protected Object createModel(AbstractEventDetectorVO<?> vo) {
		return vo.asModel();
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler#getDaoBeanName()
	 */
	@Override
	public String getDaoBeanName() {
	    return "eventDetectorDao";
	}
}
