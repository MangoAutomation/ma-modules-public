/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.DataTypes;

/**
 * @author Terry Packer
 *
 */
public enum DataTypeEnum {
	ALPHANUMERIC,
	BINARY,
	MULTISTATE,
	NUMERIC,
	IMAGE;
	
	
	/**
	 * Convert from the Mango int value to an Interval Logging type 
	 * @param type
	 * @return
	 */
	public static DataTypeEnum convertTo(int type){
		switch (type){
			case DataTypes.ALPHANUMERIC:
				return ALPHANUMERIC;
			case DataTypes.BINARY:
				return BINARY;
			case DataTypes.MULTISTATE:
				return MULTISTATE;
			case DataTypes.NUMERIC:
				return NUMERIC;
			case DataTypes.IMAGE:
				return IMAGE;
			default:
				throw new ShouldNeverHappenException("Unknown Data Type: " + type);
		}
	}
	
	/**
	 * Convert from an Interval Logging Type to the Mango int value
	 * @param type
	 * @return
	 */
	public static int convertFrom(DataTypeEnum type){
		switch (type){
		case ALPHANUMERIC:
			return DataTypes.ALPHANUMERIC;
		case BINARY:
			return DataTypes.BINARY;
		case MULTISTATE:
			return DataTypes.MULTISTATE;
		case NUMERIC:
			return DataTypes.NUMERIC;
		case IMAGE:
			return DataTypes.IMAGE;
		default:
			throw new ShouldNeverHappenException("Unknown Datat Type: " + type);
		}
	}
	
	
}
