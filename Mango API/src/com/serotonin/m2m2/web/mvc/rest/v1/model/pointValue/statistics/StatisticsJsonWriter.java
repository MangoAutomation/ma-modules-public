/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeJsonWriter;

/**
 * @author Terry Packer
 *
 */
public abstract class StatisticsJsonWriter extends PointValueTimeJsonWriter{

	protected DataPointVO vo;
	/**
	 * @param jgen
	 */
	public StatisticsJsonWriter(String host, int port, JsonGenerator jgen, DataPointVO vo, boolean useRendered, boolean unitConversion) {
		super(host, port, jgen, useRendered, unitConversion);
		this.vo = vo;
	}
	
}
