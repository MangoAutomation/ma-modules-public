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
	 * @param jgen
	 * @param dataTypeId
	 */
	public StatisticsCalculator(JsonGenerator jgen, DataPointVO vo, boolean useRendered, boolean unitConversion, long from, long to) {
		switch(vo.getPointLocator().getDataTypeId()){
			case DataTypes.BINARY:
			case DataTypes.MULTISTATE:
				this.statsGenerator = new StartsAndRuntimeListJsonGenerator(jgen, vo, useRendered, unitConversion, new StartsAndRuntimeList(from, to, null));
			break;
			case DataTypes.ALPHANUMERIC:
				this.statsGenerator = new ValueChangeCounterJsonGenerator(jgen, vo, useRendered, unitConversion, new ValueChangeCounter(from, to, null));
			break;
			case DataTypes.NUMERIC:
				this.statsGenerator = new AnalogStatisticsJsonGenerator(jgen, vo, useRendered, unitConversion, new AnalogStatistics(from, to, null));
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
	public void done(PointValueTime last) throws IOException{
		this.statsGenerator.done(last);
		
	}
	
}
