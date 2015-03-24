/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.List;

import com.infiniteautomation.mango.db.query.QueryComparison;
import com.serotonin.m2m2.db.dao.AbstractDao;
import com.serotonin.m2m2.vo.AbstractVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.PageQueryStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.VoStreamCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.query.QueryModel;

/**
 * @author Terry Packer
 *
 */
public abstract class MangoVoRestController<VO extends AbstractVO<VO>, MODEL> extends MangoRestController{

	protected AbstractDao<VO> dao;
	protected VoStreamCallback<VO, MODEL> callback;


	/**
	 * Construct a Controller using the default callback
	 * @param dao
	 */
	public MangoVoRestController(AbstractDao<VO> dao){
		this.dao = dao;
		this.callback = new VoStreamCallback<VO, MODEL>(this);
	}

	/**
	 * Construct a Controller
	 * @param dao
	 * @param callback
	 */
	public MangoVoRestController(AbstractDao<VO> dao, VoStreamCallback<VO, MODEL> callback){
		this.dao = dao;
		this.callback = callback;
	}
	
	/**
	 * Get the Query Stream for Streaming an array of data
	 * @param query
	 * @return
	 */
	protected QueryStream<VO, MODEL> getStream(QueryModel query){

		QueryStream<VO, MODEL> stream = new QueryStream<VO, MODEL>(dao, this, query, callback);
		//Ensure its ready
		stream.setupQuery();
		return stream;
	}
	
	/**
	 * Get a Stream that is more like a result set with a count
	 * @param query
	 * @return
	 */
	protected PageQueryStream<VO, MODEL> getPageStream(QueryModel query){
		PageQueryStream<VO, MODEL> stream = new PageQueryStream<VO, MODEL>(dao, this, query, callback);
		//Ensure its ready
		stream.setupQuery();
		return stream;
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
