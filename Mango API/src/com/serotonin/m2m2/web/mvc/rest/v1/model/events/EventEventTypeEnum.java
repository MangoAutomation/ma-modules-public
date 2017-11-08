/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.events;

/**
 * @author Terry Packer
 *
 */
public enum EventEventTypeEnum {
	
	ACKNOWLEDGED, //Event was acknowledged by a user
	RAISED, //Event was raised due to some alarm condition
	RETURN_TO_NORMAL, //Event was returned to normal because its cause is no longer active
	DEACTIVATED //Event was deactivated due to a deletion or termination of its source
}
