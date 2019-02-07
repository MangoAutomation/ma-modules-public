/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.db.query.StreamableSqlQuery;
import com.serotonin.m2m2.db.dao.AbstractDao;
import com.serotonin.m2m2.vo.AbstractVO;
import com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
public class QueryObjectStream <VO extends AbstractVO<VO>, MODEL, DAO extends AbstractDao<VO>> implements ObjectStream<VO>{

	
	protected DAO dao;
	protected ASTNode root;
	protected QueryStreamCallback<VO> queryCallback;
	protected MangoVoRestController<VO, MODEL, DAO> controller;
	protected StreamableSqlQuery<VO> results;
	
	/**
	 * @param query
	 * @param sort
	 * @param offset
	 * @param limit
	 * @param or
	 */
	public QueryObjectStream(DAO dao, MangoVoRestController<VO, MODEL, DAO> controller, ASTNode root, QueryStreamCallback<VO> queryCallback) {
		this.dao = dao;
		this.controller = controller;
		this.root = root;
		this.queryCallback = queryCallback;
	}

	/**
	 * Setup the Query
	 */
	public void setupQuery(){
		this.results = this.dao.createQuery(this.root, this.queryCallback, null, this.controller.getModelMap(), this.controller.getAppenders(), true);
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