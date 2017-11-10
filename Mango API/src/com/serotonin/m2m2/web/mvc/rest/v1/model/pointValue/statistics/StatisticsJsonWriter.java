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
	 * 
	 * @param host
	 * @param port
	 * @param jgen
	 * @param vo
	 * @param useRendered
	 * @param unitConversion
	 * @param dateTimeFormat - format for String dates or null for timestamp numbers
	 * @param timezone
	 */
	public StatisticsJsonWriter(String host, int port, JsonGenerator jgen, DataPointVO vo, boolean useRendered, boolean unitConversion, String dateTimeFormat, String timezone) {
		super(host, port, jgen, useRendered, unitConversion, dateTimeFormat, timezone);
		this.vo = vo;
	}
	
}
