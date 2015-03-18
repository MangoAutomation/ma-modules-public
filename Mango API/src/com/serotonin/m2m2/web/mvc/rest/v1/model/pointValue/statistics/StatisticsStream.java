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
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.JsonObjectStream;

/**
 * 
 * Class to stream statistics back via JSON
 * 
 * @author Terry Packer
 *
 */
public class StatisticsStream implements JsonObjectStream{
	
	private DataPointVO vo;
	private final boolean useRendered;
	private final boolean unitConversion;
	private final long from;
	private final long to;
	


	/**
	 * 
	 * @param vo - Data Point in question
	 * @param useRendered - Return statistics as Text rendered Strings
	 * @param from
	 * @param to
	 */
	public StatisticsStream(DataPointVO vo, boolean useRendered, boolean unitConversion, long from, long to) {
		this.vo = vo;
		this.useRendered = useRendered;
		this.unitConversion = unitConversion;
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
		
		StatisticsCalculator calculator = new StatisticsCalculator(jgen, vo, useRendered, unitConversion, this.from, this.to);

		PointValueDao dao = Common.databaseProxy.newPointValueDao();
		PointValueTime before = dao.getPointValueBefore(vo.getId(), from);
		calculator.row(before, 0);
		//Do the main work
		dao.getPointValuesBetween(vo.getId(), from, to, calculator);
		PointValueTime after = dao.getPointValueAfter(vo.getId(), to);
		//Finish
		calculator.done(after);

		
	}

}
