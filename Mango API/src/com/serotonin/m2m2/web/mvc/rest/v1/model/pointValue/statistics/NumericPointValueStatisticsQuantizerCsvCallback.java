/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import au.com.bytecode.opencsv.CSVWriter;

import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeCsvWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.RollupEnum;

/**
 * @author Terry Packer
 *
 */
public class NumericPointValueStatisticsQuantizerCsvCallback extends AbstractNumericPointValueStatisticsQuantizerCallback{


	/**
	 * 
	 * @param writer
	 * @param vo
	 * @param useRendered
	 * @param unitConversion
	 * @param rollup
	 */
	public NumericPointValueStatisticsQuantizerCsvCallback(CSVWriter writer, DataPointVO vo, 
			boolean useRendered,  boolean unitConversion, RollupEnum rollup) {
		super(new PointValueTimeCsvWriter(writer, vo, useRendered, unitConversion), rollup);
	}	
	
}
