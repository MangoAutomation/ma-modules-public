/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeCsvWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author Terry Packer
 *
 */
public class NonNumericPointValueStatisticsQuantizerCsvCallback extends AbstractNonNumericPointValueStatisticsQuantizerCallback{

	/**
	 * Write CSV without XID column
	 * 
     * @param host
     * @param port
     * @param writer
     * @param vo
     * @param useRendered
     * @param unitConversion
     * @param rollup
     * @param limit
     * @param dateTimeFormat
     * @param timezone
     */
	public NonNumericPointValueStatisticsQuantizerCsvCallback(String host, int port,
			CSVWriter writer, DataPointVO vo, boolean useRendered, 
			boolean unitConversion, RollupEnum rollup, Integer limit, String dateTimeFormat, String timezone) {
		this(host, port, writer, vo, useRendered, unitConversion, rollup, false, true, limit, dateTimeFormat, timezone);
	}

	/**
	 * 
	 * @param host
	 * @param port
	 * @param writer
	 * @param vo
	 * @param useRendered
	 * @param unitConversion
	 * @param rollup
	 * @param writeXidColumn
	 * @param writeHeaders
	 * @param limit
	 * @param dateTimeFormat
	 * @param timezone
	 */
	public NonNumericPointValueStatisticsQuantizerCsvCallback(String host, int port,
			CSVWriter writer, DataPointVO vo, boolean useRendered,
			boolean unitConversion, RollupEnum rollup, boolean writeXidColumn,
			boolean writeHeaders, Integer limit, String dateTimeFormat, String timezone) {
		super(vo, new PointValueTimeCsvWriter(host, port, writer, useRendered, unitConversion, writeXidColumn, writeHeaders, dateTimeFormat, timezone), rollup, limit);
	}
}
