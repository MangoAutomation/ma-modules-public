/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.comment;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.vo.comment.UserCommentVO;

/**
 * @author Terry Packer
 *
 */
public class UserCommentJsonStreamCallback extends UserCommentJsonWriter implements MappedRowCallback<UserCommentVO>{

	/**
	 * @param jgen
	 */
	public UserCommentJsonStreamCallback(JsonGenerator jgen) {
		super(jgen);
	}


	private final Log LOG = LogFactory.getLog(UserCommentJsonStreamCallback.class);

	
	/* (non-Javadoc)
	 * @see com.serotonin.db.MappedRowCallback#row(java.lang.Object, int)
	 */
	@Override
	public void row(UserCommentVO vo, int index) {
		try {
			this.write(vo);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		
	}

}
