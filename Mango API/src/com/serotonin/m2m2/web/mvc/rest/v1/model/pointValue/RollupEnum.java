/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

/**
 * @author Terry Packer
 *
 */
public enum RollupEnum {
	NONE(true),
	AVERAGE(false),
	DELTA(false),
	MINIMUM(false), 
	MAXIMUM(false),
    ACCUMULATOR(false),
	SUM(false), 
	FIRST(true), 
	LAST(true), 
	COUNT(true),
	INTEGRAL(false);

	private boolean nonNumericSupport; //Does this rollup support Non-Numeric point values
	private RollupEnum(boolean nonNumericSupport){
		this.nonNumericSupport = nonNumericSupport;
	}
	public boolean nonNumericSupport(){
		return this.nonNumericSupport;
	}
	
}
