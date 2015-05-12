/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.db.query.QueryModel;
import com.infiniteautomation.mango.db.query.StreamableSqlQuery;
import com.serotonin.m2m2.db.dao.AbstractDao;
import com.serotonin.m2m2.vo.AbstractVO;
import com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;

/**
 * @author Terry Packer
 *
 */
public class QueryStream<VO extends AbstractVO<VO>, MODEL> implements QueryArrayStream<VO>{

	
	protected AbstractDao<VO> dao;
	protected QueryModel query;
	protected QueryStreamCallback<VO> queryCallback;
	protected MangoVoRestController<VO, MODEL> controller;
	protected StreamableSqlQuery<VO> results;
	
	/**
	 * @param query
	 * @param sort
	 * @param offset
	 * @param limit
	 * @param or
	 */
	public QueryStream(AbstractDao<VO> dao, MangoVoRestController<VO, MODEL> controller, QueryModel query, QueryStreamCallback<VO> queryCallback) {
		this.dao = dao;
		this.controller = controller;
		this.query = query;
		this.queryCallback = queryCallback;
	}

	/**
	 * Setup the Query
	 */
	public void setupQuery(){
		this.controller.mapComparisons(this.query.getAndComparisons());
		this.controller.mapComparisons(this.query.getOrComparisons());
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

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter)
	 */
	@Override
	public void streamData(CSVPojoWriter<VO> writer) throws IOException {
		this.queryCallback.setCsvWriter(writer);
		this.results.query();
	}

	
}
