/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeJsonWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;

/**
 * @author Terry Packer
 *
 */
public class NumericPointValueStatisticsQuantizerJsonCallback extends AbstractNumericPointValueStatisticsQuantizerCallback{

	/**
	 * 
	 * @param host
	 * @param port
	 * @param jgen
	 * @param vo
	 * @param useRendered
	 * @param unitConversion
	 * @param rollup
	 * @param limit
	 * @param dateTimeFormat
	 * @param timezone
	 */
	public NumericPointValueStatisticsQuantizerJsonCallback(String host, int port, JsonGenerator jgen, DataPointVO vo, 
			boolean useRendered,  boolean unitConversion, RollupEnum rollup, Integer limit, String dateTimeFormat, String timezone) {
		super(vo, new PointValueTimeJsonWriter(host, port, jgen, useRendered, unitConversion, dateTimeFormat, timezone), rollup, limit);
	}	
	
}
