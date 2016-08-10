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
	 * @param writer
	 * @param vo
	 * @param useRendered
	 * @param unitConversion
	 * @param rollup
	 */
	public NumericPointValueStatisticsQuantizerCsvCallback(CSVWriter writer, DataPointVO vo, 
			boolean useRendered,  boolean unitConversion, RollupEnum rollup) {
		this(writer, vo, useRendered, unitConversion, rollup, false, true);
	}	
	
	/**
	 * Write a CSV with a xid column, useful for writing a sheet with multiple points (not necessarily in time order)
	 * @param writer
	 * @param vo
	 * @param useRendered
	 * @param unitConversion
	 * @param rollup
	 * @param writeXidColumn
	 */
	public NumericPointValueStatisticsQuantizerCsvCallback(CSVWriter writer, DataPointVO vo, 
			boolean useRendered,  boolean unitConversion, RollupEnum rollup, boolean writeXidColumn, boolean writeHeaders) {
		super(vo, new PointValueTimeCsvWriter(writer, vo, useRendered, unitConversion, writeXidColumn, writeHeaders), rollup);
	}
	
}
