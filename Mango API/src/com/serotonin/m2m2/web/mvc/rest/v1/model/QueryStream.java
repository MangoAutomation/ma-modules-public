/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.db.query.StreamableQuery;
import com.serotonin.m2m2.db.dao.AbstractDao;
import com.serotonin.m2m2.vo.AbstractVO;
import com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController;
import com.serotonin.m2m2.web.mvc.rest.v1.model.query.QueryModel;

/**
 * @author Terry Packer
 *
 */
public class QueryStream<VO extends AbstractVO<VO>, MODEL> implements JsonArrayStream{

	
	protected AbstractDao<VO> dao;
	protected QueryModel query;
	protected ObjectJsonStreamCallback<VO> queryCallback;
	protected MangoVoRestController<VO, MODEL> controller;
	protected StreamableQuery<VO> results;
	
	/**
	 * @param query
	 * @param sort
	 * @param offset
	 * @param limit
	 * @param or
	 */
	public QueryStream(AbstractDao<VO> dao, MangoVoRestController<VO, MODEL> controller, QueryModel query, ObjectJsonStreamCallback<VO> queryCallback) {
		this.dao = dao;
		this.controller = controller;
		this.query = query;
		this.queryCallback = queryCallback;
	}

	/**
	 * Setup the Query
	 */
	public void setupQuery(){
		this.controller.mapComparisons(this.query.getAllComparisons());
		this.results = this.dao.createQuery(query.getOrComparisons(), query.getAndComparisons(), query.getSort(), query.getOffset(), query.getLimit(), this.queryCallback, null);
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.JsonArrayStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
	 */
	@Override
	public void streamData(JsonGenerator jgen) throws IOException {
		this.queryCallback.setJsonGenerator(jgen);
		this.results.query();
	}

	
}
