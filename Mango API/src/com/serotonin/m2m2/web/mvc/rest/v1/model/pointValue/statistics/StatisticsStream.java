/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.web.mvc.rest.v1.model.JsonObjectStream;

/**
 * 
 * Class to stream statistics back via JSON
 * 
 * @author Terry Packer
 *
 */
public class StatisticsStream implements JsonObjectStream{
	
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
	public void streamData(JsonGenerator jgen) throws IOException {
		
		//TODO Can't use the Facade as there is no way to perform the callback integrated with the PointValueCache
		//PointValueFacade pointValueFacade = new PointValueFacade(this.dataPointId);
		
		StatisticsCalculator calculator = new StatisticsCalculator(jgen, this.dataTypeId, this.from, this.to);

		PointValueDao dao = Common.databaseProxy.newPointValueDao();
		PointValueTime before = dao.getPointValueBefore(dataPointId, from);
		calculator.row(before, 0);
		//Do the main work
		dao.getPointValuesBetween(dataPointId, from, to, calculator);
		PointValueTime after = dao.getPointValueAfter(dataPointId, to);
		//Finish
		calculator.done(after);

		
	}

}
