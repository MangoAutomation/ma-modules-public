/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumn;

/**
 * @author Terry Packer
 *
 */
public class XidPointValueTimeModel extends PointValueTimeModel{

	@JsonProperty("xid")
	@CSVColumn(header="xid", order = 4)
	private String xid;
	
	/**
	 * @param data
	 */
	public XidPointValueTimeModel(String xid, PointValueTime data) {
		super(data);
		this.xid = xid;
	}

	public XidPointValueTimeModel() {
		super();
	}
	
	public String getXid(){
		return this.xid;
	}
	public void setXid(String xid){
		this.xid = xid;
	}
}
