/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue;

import com.serotonin.m2m2.Common;
import com.serotonin.ShouldNeverHappenException;

/**
 * @author Terry Packer
 *
 */
public enum RollupEnum {

	
	NONE(true, Common.Rollups.NONE),
	AVERAGE(false, Common.Rollups.AVERAGE),
	DELTA(false, Common.Rollups.DELTA),
	MINIMUM(false, Common.Rollups.MINIMUM), 
	MAXIMUM(false, Common.Rollups.MAXIMUM),
    ACCUMULATOR(false, Common.Rollups.ACCUMULATOR),
	SUM(false, Common.Rollups.SUM), 
	FIRST(true, Common.Rollups.FIRST), 
	LAST(true, Common.Rollups.LAST), 
	COUNT(true, Common.Rollups.COUNT),
	INTEGRAL(false, Common.Rollups.INTEGRAL),
	FFT(false, -1),
	ALL(true, Common.Rollups.ALL),
    START(true, Common.Rollups.START),
    POINT_DEFAULT(true, -2);

	private boolean nonNumericSupport; //Does this rollup support Non-Numeric point values
	private int id;
	
	private RollupEnum(boolean nonNumericSupport, int id){
		this.nonNumericSupport = nonNumericSupport;
		this.id = id;
	}
	public boolean nonNumericSupport(){
		return this.nonNumericSupport;
	}
	public int getId(){
		return this.id;
	}
	
	public static RollupEnum convertTo(int id){
		for(RollupEnum r : RollupEnum.values())
			if(r.id == id)
				return r;
		
		throw new ShouldNeverHappenException("Unknown Rollup, id: " + id);
	}
	
	/**
	 * Convert from an ENUM String to an ID
	 * if none is found return -1
	 * @param code
	 * @return
	 */
	public static int getFromCode(String code){
		for(RollupEnum r : RollupEnum.values())
			if(r.name().equals(code))
				return r.id;
		return -1;
	}
}
