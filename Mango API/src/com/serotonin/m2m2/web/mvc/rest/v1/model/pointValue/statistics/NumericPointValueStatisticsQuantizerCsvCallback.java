/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import au.com.bytecode.opencsv.CSVWriter;

import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeCsvWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;

/**
 * @author Terry Packer
 *
 */
public class NumericPointValueStatisticsQuantizerCsvCallback extends AbstractNumericPointValueStatisticsQuantizerCallback{


	/**
	 * Write CSV with no XID Column
     * 
     * @param writer
     * @param vo
     * @param useRendered
     * @param unitConversion
     * @param rollup
     * @param limit
     * @param dateTimeFormat
     * @param timezone
     */
	public NumericPointValueStatisticsQuantizerCsvCallback(CSVWriter writer, DataPointVO vo, 
			boolean useRendered,  boolean unitConversion, RollupEnum rollup, Integer limit, String dateTimeFormat, String timezone) {
		this(writer, vo, useRendered, unitConversion, rollup, false, true, limit, dateTimeFormat, timezone);
	}	
	
	/**
	 * Write a CSV with a xid column, useful for writing a sheet with multiple points (not necessarily in time order)
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
	public NumericPointValueStatisticsQuantizerCsvCallback(CSVWriter writer, DataPointVO vo, 
			boolean useRendered,  boolean unitConversion, RollupEnum rollup, boolean writeXidColumn, boolean writeHeaders, Integer limit, String dateTimeFormat, String timezone) {
		super(vo, new PointValueTimeCsvWriter(writer, useRendered, unitConversion, writeXidColumn, writeHeaders, dateTimeFormat, timezone), rollup, limit);
	}
	
}
