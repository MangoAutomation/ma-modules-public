/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.ObjectStream;

/**
 * 
 * Class to stream statistics back via JSON
 * 
 * @author Terry Packer
 *
 */
public class StatisticsStream implements ObjectStream<PointValueTime>{
	
	private DataPointVO vo;
	private final boolean useRendered;
	private final boolean unitConversion;
	private final long from;
	private final long to;
	private final String dateTimeFormat;
	private final String timezone;

	/**
	 * 
	 * @param vo - Data Point in question
	 * @param useRendered - Return statistics as Text rendered Strings
	 * @param unitConversion
	 * @param from
	 * @param to
	 * @param dateTimeFormat - format for String dates or null for timestamp numbers
	 * @param timezone
	 */
	public StatisticsStream(DataPointVO vo, boolean useRendered, boolean unitConversion, long from, long to, String dateTimeFormat, String timezone) {
		this.vo = vo;
		this.useRendered = useRendered;
		this.unitConversion = unitConversion;
		this.from = from;
		this.to = to;
		this.dateTimeFormat = dateTimeFormat;
		this.timezone = timezone;
	}

	
	
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
	 */
	@Override
	public void streamData(JsonGenerator jgen) throws IOException {
		
		//TODO Can't use the Facade as there is no way to perform the callback integrated with the PointValueCache
		//PointValueFacade pointValueFacade = new PointValueFacade(this.dataPointId);
		
		//First find the start value
	    PointValueDao pvd = Common.databaseProxy.newPointValueDao();
		PointValueTime startPvt  = pvd.getPointValueBefore(vo.getId(), from);
		DataValue startValue = null;
		if(startPvt != null)
			startValue = startPvt.getValue();
		StatisticsCalculator calculator = new StatisticsCalculator(jgen, vo, useRendered, unitConversion, this.from, this.to, startValue, dateTimeFormat, timezone);

		//Do the main work
		pvd.getPointValuesBetween(vo.getId(), from, to, calculator);
		//Finish
		calculator.done();
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.ObjectStream#streamData(com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter)
	 */
	@Override
	public void streamData(CSVPojoWriter<PointValueTime> jgen)
			throws IOException {
		throw new ShouldNeverHappenException("Un-implemented");
		
	}

}
