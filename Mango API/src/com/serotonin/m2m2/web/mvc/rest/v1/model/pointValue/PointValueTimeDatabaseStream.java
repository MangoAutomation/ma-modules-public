/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.web.mvc.rest.v1.model.JsonArrayStream;

/**
 * @author Terry Packer
 *
 */
public class PointValueTimeDatabaseStream implements JsonArrayStream{

	private int dataPointId;
	private long from;
	private long to;
	private PointValueDao dao;
	
	/**
	 * @param id
	 * @param from
	 * @param to
	 */
	public PointValueTimeDatabaseStream(int dataPointId, long from, long to, PointValueDao dao) {
		this.dataPointId = dataPointId;
		this.from = from;
		this.to = to;
		this.dao = dao;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
	 */
	@Override
	public void streamData(JsonGenerator jgen) {
		this.dao.getPointValuesBetween(dataPointId, from, to, new PointValueTimeJsonStreamCallback(jgen));
	}

}
