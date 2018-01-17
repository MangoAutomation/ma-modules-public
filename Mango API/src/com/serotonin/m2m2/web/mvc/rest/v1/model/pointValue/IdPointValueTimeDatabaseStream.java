/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream;

/**
 * Stream Point Values in 2 formats:
 * 
 * JSON:multiple values per time entry in one large array
 * 
 * CSV: 1 point per column
 * 
 * 
 * @author Terry Packer
 *
 */
public class IdPointValueTimeDatabaseStream implements QueryArrayStream<PointValueTimeModel>{

	private boolean useRendered;
	private boolean unitConversion;
	private long from;
	private long to;
	private PointValueDao dao;
	private final Integer limit;
	private final String dateTimeFormat; //Dates or Time stamps
	private final String timezone;

	private final Map<Integer,DataPointVO> pointMap;
	

	/**
	 * 
	 * @param pointMap
	 * @param useRendered
	 * @param unitConversion
	 * @param from
	 * @param to
	 * @param dao
	 * @param limit
	 * @param dateTimeFormat - format for String dates or null for timestamp numbers
	 * @param timezone
	 */
	public IdPointValueTimeDatabaseStream(Map<Integer,DataPointVO> pointMap, boolean useRendered,  boolean unitConversion, long from, long to, PointValueDao dao, Integer limit, String dateTimeFormat, String timezone) {
		this.pointMap = pointMap;
		this.useRendered = useRendered;
		this.unitConversion = unitConversion;
		this.from = from;
		this.to = to;
		this.dao = dao;
		this.limit = limit;
		this.dateTimeFormat = dateTimeFormat;
		this.timezone = timezone;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
	 */
	@Override
	public void streamData(JsonGenerator jgen) {
		IdPointValueTimeJsonStreamCallback callback = new IdPointValueTimeJsonStreamCallback(jgen, pointMap, useRendered, unitConversion, limit, dateTimeFormat, timezone);
		this.dao.getPointValuesBetween(new ArrayList<Integer>(pointMap.keySet()), from, to, callback);
		callback.finish();
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter)
	 */
	@Override
	public void streamData(CSVPojoWriter<PointValueTimeModel> writer)
			throws IOException {
		IdPointValueTimeCsvStreamCallback callback = new IdPointValueTimeCsvStreamCallback(writer.getWriter(), pointMap, useRendered, unitConversion, limit, dateTimeFormat, timezone);
		this.dao.getPointValuesBetween(new ArrayList<Integer>(pointMap.keySet()), from, to, callback);
		callback.finish();
	}

}
