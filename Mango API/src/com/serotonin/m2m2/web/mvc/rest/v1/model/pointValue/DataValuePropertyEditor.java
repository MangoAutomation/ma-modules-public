/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPropertyEditor;

/**
 * @author Terry Packer
 *
 */
public class DataValuePropertyEditor extends CSVPropertyEditor{

	private Object dataValue; //Currently only an Object
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPropertyEditor#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(Object value) {
		this.dataValue = value;
		
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPropertyEditor#getValue()
	 */
	@Override
	public Object getValue() {
		return dataValue;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPropertyEditor#getAsText()
	 */
	@Override
	public String getAsText() {
		return dataValue.toString();
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPropertyEditor#setAsText(java.lang.String)
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		//Not happening
	}

}
