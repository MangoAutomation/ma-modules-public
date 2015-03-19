/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.comment;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.db.dao.QueryParameter;
import com.serotonin.m2m2.db.dao.UserCommentDao;
import com.serotonin.m2m2.vo.comment.UserCommentVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.JsonArrayStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.query.QueryModel;

/**
 * @author Terry Packer
 *
 */
public class UserCommentDatabaseStream implements JsonArrayStream{


	private QueryModel query;

	
	
	
	/**
	 * @param query
	 * @param sort
	 * @param offset
	 * @param limit
	 * @param or
	 */
	public UserCommentDatabaseStream(QueryModel query) {
		super();
		this.query = query;
	}



	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.JsonArrayStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
	 */
	@Override
	public void streamData(JsonGenerator jgen) throws IOException {
		
		//Check for the attribute commentType
		if(query.getQuery() !=null){
			for(QueryParameter param : query.getQuery()){
				if(param.getAttribute().equalsIgnoreCase("commentType")){
					param.setCondition(Integer.toString(UserCommentVO.COMMENT_TYPE_CODES.getId(param.getCondition())));
				}
			}
		}
		UserCommentDao.instance.streamQuery(query.getQuery(), query.getSort(), query.getOffset(), query.getLimit(), query.isUseOr(), new UserCommentJsonStreamCallback(jgen));
	}

}
