/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.eventHandler;

import com.serotonin.m2m2.db.dao.EventHandlerDao;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.AbstractEventHandlerVO;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController;
import com.serotonin.m2m2.web.mvc.rest.v1.model.FilteredVoStreamCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.handlers.AbstractEventHandlerModel;

/**
 * Filter Event Handlers Based on Permissions for thier Event Type
 * @author Terry Packer
 */
public class EventHandlerStreamCallback extends FilteredVoStreamCallback<AbstractEventHandlerVO<?>, AbstractEventHandlerModel<?>, EventHandlerDao>{

	private User user;
	
	/**
	 * @param controller
	 * @param user
	 * 
	 */
	public EventHandlerStreamCallback(
			MangoVoRestController<AbstractEventHandlerVO<?>, AbstractEventHandlerModel<?>, EventHandlerDao> controller,
			User user) {
		super(controller);
		this.user = user;

	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.FilteredQueryStreamCallback#filter(java.lang.Object)
	 */
	@Override
	protected boolean filter(AbstractEventHandlerVO<?> vo) {
		if(vo.getEventType() != null)
			return !Permissions.hasEventTypePermission(user,vo.getEventType());
		else
			return false;
	}
	
}
