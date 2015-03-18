/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.comment;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.vo.UserComment;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractRestModel;

/**
 * @author Terry Packer
 *
 */
public class UserCommentModel extends AbstractRestModel<UserComment>{

	/**
	 * @param data
	 */
	public UserCommentModel(UserComment comment) {
		super(comment);
	}

	public UserCommentModel(){
		super(new UserComment());
	}
	
	@JsonGetter
	public int getUserId(){
		return this.data.getUserId();
	}
	@JsonSetter
	public void setUserId(int userId){
		this.data.setUserId(userId);
	}
	
	@JsonGetter
	public String getUsername(){
		return this.data.getUsername();
	}
	@JsonSetter
	public void setUsername(String username){
		this.data.setUsername(username);
	}

	@JsonGetter
	public String getComment(){
		return this.data.getComment();
	}
	@JsonSetter
	public void setComment(String comment){
		this.data.setComment(comment);
	}
	
	@JsonGetter
	public long getTimestamp(){
		return this.data.getTs();
	}
	@JsonSetter
	public void setTimestamp(long timestamp){
		this.data.setTs(timestamp);
	}
}
