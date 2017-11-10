/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.stats.AnalogStatistics;
import com.serotonin.m2m2.view.stats.StartsAndRuntimeList;
import com.serotonin.m2m2.view.stats.ValueChangeCounter;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * @author Terry Packer
 *
 */
public class StatisticsCalculator implements MappedRowCallback<PointValueTime>{

	private StatisticsJsonGenerator statsGenerator;
	
	/**
	 * 
	 * @param host
	 * @param port
	 * @param jgen
	 * @param vo
	 * @param useRendered
	 * @param unitConversion
	 * @param from
	 * @param to
	 * @param startValue
	 * @param dateTimeFormat Data Point in question
	 */
	public StatisticsCalculator(String host, int port, JsonGenerator jgen, DataPointVO vo, boolean useRendered, boolean unitConversion, long from, long to, DataValue startValue, String dateTimeFormat, String timezone) {
		switch(vo.getPointLocator().getDataTypeId()){
			case DataTypes.BINARY:
			case DataTypes.MULTISTATE:
				this.statsGenerator = new StartsAndRuntimeListJsonGenerator(host, port, jgen, vo, useRendered, unitConversion, new StartsAndRuntimeList(from, to, startValue), dateTimeFormat, timezone);
			break;
			case DataTypes.ALPHANUMERIC:
			case DataTypes.IMAGE:
				this.statsGenerator = new ValueChangeCounterJsonGenerator(host, port, jgen, vo, useRendered, unitConversion, new ValueChangeCounter(from, to, startValue), dateTimeFormat, timezone);
			break;
			case DataTypes.NUMERIC:
				this.statsGenerator = new AnalogStatisticsJsonGenerator(host, port, jgen, vo, useRendered, unitConversion, new AnalogStatistics(from, to, startValue == null ? null : startValue.getDoubleValue()), dateTimeFormat, timezone);
			break;
			default:
				throw new ShouldNeverHappenException("Invalid Data Type: "+ vo.getPointLocator().getDataTypeId());
		}
	}

	/* (non-Javadoc)
	 * @see com.serotonin.db.MappedRowCallback#row(java.lang.Object, int)
	 */
	@Override
	public void row(PointValueTime pvt, int index) {
		if(pvt == null)
			return; //We never add null PVTs
		this.statsGenerator.addValueTime(pvt);
	}

	/**
	 * Signal we are finished
	 * @throws IOException 
	 */
	public void done() throws IOException{
		this.statsGenerator.done();
	}
	
}
