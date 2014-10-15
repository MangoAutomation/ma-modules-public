/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeJsonWriter;

/**
 * @author Terry Packer
 *
 */
public abstract class StatisticsJsonWriter extends PointValueTimeJsonWriter{

	/**
	 * @param jgen
	 */
	public StatisticsJsonWriter(JsonGenerator jgen) {
		super(jgen);
	}
	
}
