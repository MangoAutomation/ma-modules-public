/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.PointValueFacade;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.view.stats.AnalogStatistics;
import com.serotonin.m2m2.view.stats.StartsAndRuntime;
import com.serotonin.m2m2.view.stats.StartsAndRuntimeList;
import com.serotonin.m2m2.view.stats.ValueChangeCounter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeStream;

/**
 * 
 * Class to stream statistics back via JSON
 * 
 * @author Terry Packer
 *
 */
public class StatisticsStream implements PointValueTimeStream{
	
	private int dataPointId;
	private int dataTypeId;
	private long from;
	private long to;
	

	/**
	 * @param id
	 * @param dataTypeId
	 * @param time
	 * @param time2
	 */
	public StatisticsStream(int dataPointId, int dataTypeId, long from, long to) {
		this.dataPointId = dataPointId;
		this.dataTypeId = dataTypeId;
		this.from = from;
		this.to = to;
	}

	
	
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
	 */
	@Override
	public void streamData(JsonGenerator jgen) {
		
		PointValueFacade pointValueFacade = new PointValueFacade(this.dataPointId);
		
		StatisticsCalculator calculator = new StatisticsCalculator(this.dataTypeId, this.from, this.to);
		
		//Do Process the values with Callbacks
		//TODO Need to implement Callbacks for PointValue Facade
		//pointValueFacade.getPointValuesBetween(from, to, true, true, calculator);
		
		calculator.done(jgen);

		
	}

}
