/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.List;

import com.infiniteautomation.mango.db.query.QueryComparison;
import com.serotonin.m2m2.db.dao.AbstractDao;
import com.serotonin.m2m2.vo.AbstractVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RqlQueryStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.VoJsonStreamCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.query.QueryModel;

/**
 * @author Terry Packer
 *
 */
public abstract class MangoVoRestController<VO extends AbstractVO<VO>, MODEL> extends MangoRestController{

	protected AbstractDao<VO> dao;
	protected VoJsonStreamCallback<VO, MODEL> callback;


	/**
	 * Construct a Controller using the default callback
	 * @param dao
	 */
	public MangoVoRestController(AbstractDao<VO> dao){
		this.dao = dao;
		this.callback = new VoJsonStreamCallback<VO, MODEL>(this);
	}

	/**
	 * Construct a Controller
	 * @param dao
	 * @param callback
	 */
	public MangoVoRestController(AbstractDao<VO> dao, VoJsonStreamCallback<VO, MODEL> callback){
		this.dao = dao;
		this.callback = callback;
	}
	
	/**
	 * Get the Query Stream
	 * @param query
	 * @return
	 */
	protected RqlQueryStream<VO, MODEL> getStream(QueryModel query){
		 return new RqlQueryStream<VO, MODEL>(dao, this, query, callback);
	}
	

	/**
	 * Map any Model members to VO Properties
	 * @param list
	 */
	public abstract void mapComparisons(List<QueryComparison> list);
	
	/**
	 * Create a Model
	 * @param vo
	 * @return
	 */
	public abstract MODEL createModel(VO vo);
	
}
