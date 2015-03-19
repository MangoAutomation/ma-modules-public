/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.comment;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.vo.comment.UserCommentVO;

/**
 * @author Terry Packer
 *
 */
public class UserCommentJsonWriter {

	protected JsonGenerator jgen;

	public UserCommentJsonWriter(JsonGenerator jgen){
		this.jgen = jgen;
	}
	
	protected void write(UserCommentVO vo) throws IOException{
		UserCommentModel model = new UserCommentModel(vo);
		jgen.writeObject(model);
	}
}
