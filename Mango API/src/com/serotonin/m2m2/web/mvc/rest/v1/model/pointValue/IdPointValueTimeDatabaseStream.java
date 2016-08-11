/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
	private HttpServletRequest request;
	private final Map<Integer,DataPointVO> pointMap;
	
	/**
	 * @param id
	 * @param from
	 * @param to
	 */
	public IdPointValueTimeDatabaseStream(HttpServletRequest request, Map<Integer,DataPointVO> pointMap, boolean useRendered,  boolean unitConversion, long from, long to, PointValueDao dao) {
		this.request = request;
		this.pointMap = pointMap;
		this.useRendered = useRendered;
		this.unitConversion = unitConversion;
		this.from = from;
		this.to = to;
		this.dao = dao;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
	 */
	@Override
	public void streamData(JsonGenerator jgen) {
		IdPointValueTimeJsonStreamCallback callback = new IdPointValueTimeJsonStreamCallback(request, jgen, pointMap, useRendered, unitConversion);
		this.dao.getPointValuesBetween(new ArrayList<Integer>(pointMap.keySet()), from, to, callback);
		callback.finish();
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter)
	 */
	@Override
	public void streamData(CSVPojoWriter<PointValueTimeModel> writer)
			throws IOException {
		IdPointValueTimeCsvStreamCallback callback = new IdPointValueTimeCsvStreamCallback(request, writer.getWriter(), pointMap, useRendered, unitConversion);
		this.dao.getPointValuesBetween(new ArrayList<Integer>(pointMap.keySet()), from, to, callback);
		callback.finish();
	}

}
