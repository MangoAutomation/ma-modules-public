/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.db.query.QueryComparison;
import com.serotonin.m2m2.db.dao.AbstractDao;
import com.serotonin.m2m2.vo.AbstractVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.query.QueryModel;

/**
 * @author Terry Packer
 *
 */
public class RqlQueryStream<T extends AbstractVO<T>> implements JsonArrayStream{

	
	protected AbstractDao<T> dao;
	protected QueryModel query;
	protected JsonStreamCallback<T> callback;
	
	/**
	 * @param query
	 * @param sort
	 * @param offset
	 * @param limit
	 * @param or
	 */
	public RqlQueryStream(AbstractDao<T> dao, QueryModel query, JsonStreamCallback<T> callback) {
		this.dao = dao;
		this.query = query;
		this.callback = callback;
	}

	public RqlQueryStream(AbstractDao<T> dao, QueryModel query){
		this.dao = dao;
		this.query = query;
		this.callback = new JsonStreamCallback<T>();
	}
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.JsonArrayStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
	 */
	@Override
	public void streamData(JsonGenerator jgen) throws IOException {
		this.mapComparisons(query.getAllComparisons());
		this.callback.setJsonGenerator(jgen);
		this.dao.streamQuery(query.getOrComparisons(), query.getAndComparisons(), query.getSort(), query.getOffset(), query.getLimit(), this.callback);
	}
	
	/**
	 * Map any values to DB types, override as necessary
	 * @param list
	 */
	public void mapComparisons(List<QueryComparison> list){ }
	
}
