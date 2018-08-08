/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.publisher;

import org.springframework.stereotype.Component;

import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.comment.UserCommentVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.comment.UserCommentModel;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 * @author Terry Packer
 *
 */
@Component("userCommentWebSocketHandler")
public class UserCommentWebSocketHandler extends DaoNotificationWebSocketHandler<UserCommentVO>{

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler#hasPermission(com.serotonin.m2m2.vo.User, java.lang.Object)
	 */
	@Override
	protected boolean hasPermission(User user, UserCommentVO vo) {
		if(user.hasAdminPermission())
			return true;
		else 
			return user.getId() == vo.getUserId();
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler#createModel(java.lang.Object)
	 */
	@Override
	protected Object createModel(UserCommentVO vo) {
		return new UserCommentModel(vo);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler#getDaoBeanName()
	 */
	@Override
	public String getDaoBeanName() {
	    return "userCommentDao";
	}
}
