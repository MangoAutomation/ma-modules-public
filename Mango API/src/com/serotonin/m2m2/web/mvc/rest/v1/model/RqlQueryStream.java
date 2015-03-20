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
import com.serotonin.m2m2.web.mvc.rest.v1.model.query.QueryModel;

/**
 * @author Terry Packer
 *
 */
public class RqlQueryStream<VO extends AbstractVO<VO>, MODEL> implements JsonArrayStream{

	
	protected AbstractDao<VO> dao;
	protected QueryModel query;
	protected AbstractJsonStreamCallback<VO> callback;
	protected MangoVoRestController<VO, MODEL> controller;
	
	/**
	 * @param query
	 * @param sort
	 * @param offset
	 * @param limit
	 * @param or
	 */
	public RqlQueryStream(AbstractDao<VO> dao, MangoVoRestController<VO, MODEL> controller, QueryModel query, AbstractJsonStreamCallback<VO> callback) {
		this.dao = dao;
		this.controller = controller;
		this.query = query;
		this.callback = callback;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.JsonArrayStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
	 */
	@Override
	public void streamData(JsonGenerator jgen) throws IOException {
		this.controller.mapComparisons(query.getAllComparisons());
		this.callback.setJsonGenerator(jgen);
		this.dao.streamQuery(query.getOrComparisons(), query.getAndComparisons(), query.getSort(), query.getOffset(), query.getLimit(), this.callback);
	}

	
}
