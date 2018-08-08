/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.publisher;

import org.springframework.stereotype.Component;

import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.publish.PublisherVO;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 * @author Terry Packer
 *
 */
@Component("publisherWebSocketHandler")
public class PublisherWebSocketHandler extends DaoNotificationWebSocketHandler<PublisherVO<?>>{

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler#hasPermission(com.serotonin.m2m2.vo.User, java.lang.Object)
	 */
	@Override
	protected boolean hasPermission(User user, PublisherVO<?> vo) {
		if(user.hasAdminPermission())
			return true;
		else 
			return false; //TODO Implement permissions for publishers... Permissions.hasPublisherPermission(user, vo);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler#createModel(java.lang.Object)
	 */
	@Override
	protected Object createModel(PublisherVO<?> vo) {
		return vo.asModel();
	}

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler#getDaoClass()
     */
    @Override
    public String getDaoBeanName() {
        return "publisherDao";
    }

}
