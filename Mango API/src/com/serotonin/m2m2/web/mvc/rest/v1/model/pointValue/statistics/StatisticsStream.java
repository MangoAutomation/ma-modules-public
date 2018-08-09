/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.rt.dataImage.PointValueFacade;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
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
		
		PointValueFacade point = new PointValueFacade(vo.getId());
        PointValueTime start = point.getPointValueBefore(from + 1);
        List<PointValueTime> values = point.getPointValuesBetween(from + 1, to);
        if(start != null && start.getTime() == from)
            values.add(0, start);
        
		StatisticsCalculator calculator = new StatisticsCalculator(jgen, vo, useRendered, unitConversion, this.from, this.to, start, values, dateTimeFormat, timezone);
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
