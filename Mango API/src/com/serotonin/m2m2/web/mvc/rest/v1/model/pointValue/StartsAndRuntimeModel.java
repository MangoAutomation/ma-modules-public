/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.serotonin.m2m2.view.stats.StartsAndRuntime;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractRestModel;


/**
 * @author Terry Packer
 *
 */
public class StartsAndRuntimeModel extends AbstractRestModel<StartsAndRuntime>{

	public StartsAndRuntimeModel(){
		super(new StartsAndRuntime());
	}
	
	/**
	 * @param data
	 */
	public StartsAndRuntimeModel(StartsAndRuntime data) {
		super(data);
	}
	
	@JsonGetter("value")
	public Object getValue(){
		return this.data.getValue();
	}
	@JsonGetter("runtime")
	public long getRuntime(){
		return this.data.getRuntime();
	}
	@JsonGetter("proportion")
	public double getProportion(){
		return this.data.getProportion();
	}
	@JsonGetter("starts")
	public int getStarts(){
		return this.data.getStarts();
	}
}
