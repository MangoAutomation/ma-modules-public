/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.publisher;

import org.springframework.stereotype.Component;

import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.user.UserModel;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 * @author Terry Packer
 *
 */
@Component("userWebSocketHandler")
public class UserWebSocketHandler extends DaoNotificationWebSocketHandler<User>{
    
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler#hasPermission(com.serotonin.m2m2.vo.User, java.lang.Object)
	 */
	@Override
	protected boolean hasPermission(User user, User vo) {
		if(user.hasAdminPermission())
			return true;
		else 
			return user.getId() == vo.getId();
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler#createModel(java.lang.Object)
	 */
	@Override
	protected Object createModel(User vo) {
		return new UserModel(vo);
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler#getDaoBeanName()
	 */
	@Override
	public String getDaoBeanName() {
	    return "userDao";
	}
}
