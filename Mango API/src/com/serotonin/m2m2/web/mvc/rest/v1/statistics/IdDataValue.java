/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.statistics;

import com.serotonin.m2m2.rt.dataImage.types.DataValue;

/**
 * @author Terry Packer
 *
 */
public class IdDataValue {
	
	private int id;
	private DataValue value;
	
	public IdDataValue(int id, DataValue value){
		this.id = id;
		this.value = value;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public DataValue getValue() {
		return value;
	}

	public void setValue(DataValue value) {
		this.value = value;
	}

}
