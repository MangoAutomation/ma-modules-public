/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.comment;

import java.util.List;

import com.infiniteautomation.mango.db.query.QueryComparison;
import com.serotonin.m2m2.db.dao.UserCommentDao;
import com.serotonin.m2m2.vo.comment.UserCommentVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RqlQueryStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.query.QueryModel;

/**
 * @author Terry Packer
 *
 */
public class UserCommentDatabaseStream extends RqlQueryStream<UserCommentVO>{

	
	/**
	 * 
	 * @param query
	 */
	public UserCommentDatabaseStream(QueryModel query) {
		super(UserCommentDao.instance, query);
		this.query = query;
	}


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.RqlQueryStream#mapComparisons(java.util.List)
	 */
	@Override
	public void mapComparisons(List<QueryComparison> list) {
		//Check for the attribute commentType
		for(QueryComparison param : list){
			if(param.getAttribute().equalsIgnoreCase("commentType")){
				param.setCondition(Integer.toString(UserCommentVO.COMMENT_TYPE_CODES.getId(param.getCondition())));
			}
		}
	}

}
