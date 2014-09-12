/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.DataTypes;

/**
 * Enum Model for com.serotonin.m2m2.DataTypes
 * @author Terry Packer
 *
 */
public enum DataTypeEnum {
	
	UNKNOWN,
	BINARY,
	MULTISTATE,
	NUMERIC,
	ALPHANUMERIC,
	IMAGE;
	
	/**
	 * Convert from Mango DataType value to DataTypeEnum
	 * @See com.serotonin.m2m2.DataTypes
	 * @param type
	 * @return
	 */
	public static DataTypeEnum convertTo(int type){
		switch(type){
			case DataTypes.UNKNOWN:
				return UNKNOWN;
			case DataTypes.BINARY:
				return BINARY;
			case DataTypes.MULTISTATE:
				return MULTISTATE;
			case DataTypes.NUMERIC:
				return NUMERIC;
			case DataTypes.ALPHANUMERIC:
				return ALPHANUMERIC;
			case DataTypes.IMAGE:
				return IMAGE;
			default:
				throw new ShouldNeverHappenException("Unknown data type value: " + type);
		}
	}
	
	/**
	 * Convert from DataTypeEnum to Mango data type value
	 * @See com.serotonin.m2m2.DataTypes
	 * @param type
	 * @return
	 */
	public static int convertFrom(DataTypeEnum type){
		switch(type){
			case UNKNOWN:
				return DataTypes.UNKNOWN;
			case BINARY:
				return DataTypes.BINARY;
			case MULTISTATE:
				return DataTypes.MULTISTATE;
			case NUMERIC:
				return DataTypes.NUMERIC;
			case ALPHANUMERIC:
				return DataTypes.ALPHANUMERIC;
			case IMAGE:
				return DataTypes.IMAGE;
			default:
				throw new ShouldNeverHappenException("Unknown data type value: " + type);
		}
	}

}
