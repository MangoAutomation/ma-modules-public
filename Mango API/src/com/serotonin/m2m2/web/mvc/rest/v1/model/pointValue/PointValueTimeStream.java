/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * @author Terry Packer
 *
 */
public interface PointValueTimeStream {
	
	public abstract void streamData(JsonGenerator jgen);
	
}
