/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.thread;

/**
 * @author Terry Packer
 *
 */
public enum ThreadModelProperty {
	
	ID,NAME,CPU_TIME,USER_TIME,STATE,PRIORITY,LOCATION;

	/**
     */
	public static ThreadModelProperty convert(String orderBy) {
		
		if(orderBy.equalsIgnoreCase("location"))
			return LOCATION;
		else if(orderBy.equalsIgnoreCase("name"))
			return NAME;
		else if(orderBy.equalsIgnoreCase("cpuTime"))
			return CPU_TIME;
		else if(orderBy.equalsIgnoreCase("userTime"))
			return USER_TIME;
		else if(orderBy.equalsIgnoreCase("state"))
			return STATE;
		else if(orderBy.equalsIgnoreCase("priority"))
			return PRIORITY;
		else
			return ID;
		
		
	}
	
	
	
}
