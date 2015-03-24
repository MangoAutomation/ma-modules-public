/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.db.dao.AbstractDao;
import com.serotonin.m2m2.vo.AbstractVO;
import com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.query.QueryModel;

/**
 * @author Terry Packer
 *
 */
public class PageQueryStream<VO extends AbstractVO<VO>, MODEL> extends QueryStream<VO,MODEL> implements QueryDataPageStream<VO>{

	
	protected QueryStreamCallback<Long> countCallback;
	

	/**
	 * 
	 * @param dao
	 * @param controller
	 * @param query
	 * @param queryCallback
	 */
	public PageQueryStream(AbstractDao<VO> dao, MangoVoRestController<VO, MODEL> controller, QueryModel query, QueryStreamCallback<VO> queryCallback) {
		super(dao, controller, query, queryCallback);
		this.countCallback = new QueryStreamCallback<Long>();
	}

	/**
	 * Setup the Query
	 */
	@Override
	public void setupQuery(){
		this.controller.mapComparisons(this.query.getAllComparisons());
		this.results = this.dao.createQuery(query.getOrComparisons(), query.getAndComparisons(), query.getSort(), query.getOffset(), query.getLimit(), this.queryCallback, this.countCallback);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.JsonDataPageStream#streamCount(com.fasterxml.jackson.core.JsonGenerator)
	 */
	@Override
	public void streamCount(JsonGenerator jgen) throws IOException {
		this.countCallback.setJsonGenerator(jgen);
		this.results.count();
		
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryDataPageStream#streamCount(com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter)
	 */
	@Override
	public void streamCount(CSVPojoWriter<Long> writer) throws IOException {		
		//Currently doing nothing as this would create a weird CSV
	}

	
}
