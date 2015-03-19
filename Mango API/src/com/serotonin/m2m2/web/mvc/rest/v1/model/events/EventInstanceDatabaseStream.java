/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.events;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.db.dao.EventInstanceDao;
import com.serotonin.m2m2.web.mvc.rest.v1.model.JsonArrayStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.query.QueryModel;

/**
 * @author Terry Packer
 *
 */
public class EventInstanceDatabaseStream implements JsonArrayStream{

	private QueryModel query;
	
	/**
	 * @param query
	 * @param sort
	 * @param offset
	 * @param limit
	 * @param or
	 */
	public EventInstanceDatabaseStream(QueryModel query) {
		super();
		this.query = query;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.JsonArrayStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
	 */
	@Override
	public void streamData(JsonGenerator jgen) throws IOException {
		EventInstanceDao.instance.streamQuery(query.getQuery(), query.getSort(), query.getOffset(), query.getLimit(), query.isUseOr(), new EventInstanceJsonStreamCallback(jgen));
	}

}
