/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DaoRegistry;
import com.serotonin.m2m2.vo.DataPointSummary;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

/**
 * @author Terry Packer
 *
 */
public class DataPointSummaryModel extends AbstractRestModel<DataPointSummary>{

	public DataPointSummaryModel(){
		this(new DataPointSummary(), null);
	}
	
	//For performance
	private String dataSourceXid;
	
	/**
	 * @param data
	 * @param dataSourceXid - for performance
	 */
	public DataPointSummaryModel(DataPointSummary data, String dataSourceXid) {
		super(data);
		this.dataSourceXid = dataSourceXid;
	}

	@JsonGetter("xid")
	public String getXid(){
		return this.data.getXid();
	}
	@JsonSetter("xid")
	public void setXid(String xid){
		this.data.setXid(xid);
	}
	
	@JsonGetter("name")
	public String getName(){
		return this.data.getName();
	}
	@JsonSetter("name")
	public void setName(String name){
		this.data.setName(name);
	}
	
	@JsonGetter("dataSourceXid")
	public String getDataSourceXid(){
		if(this.dataSourceXid != null)
			return this.dataSourceXid;
		DataSourceVO<?> ds = DaoRegistry.dataSourceDao.getDataSource(this.data.getDataSourceId());
		if(ds != null){
			return ds.getXid();
		}else{
			return null;
		}
	}
	@JsonSetter("dataSourceXid")
	public void setDataSourceXid(String dataSouceXid){
		DataSourceVO<?> ds = DaoRegistry.dataSourceDao.getDataSource(this.data.getDataSourceId());
		if(ds != null){
			this.data.setDataSourceId(ds.getId());
		}else{
			this.data.setDataSourceId(Common.NEW_ID);
		}
	}
	
	@JsonGetter("deviceName")
	public String getDeviceName(){
		return this.data.getDeviceName();
	}
	@JsonSetter("deviceName")
	public void setDeviceName(String deviceName){
		this.data.setDeviceName(deviceName);
	}

	/*
	 * There is no point folder XID so we must use the ID
	 */
	@JsonGetter("pointFolderId")
	public int getPointFolderId(){
		return this.data.getPointFolderId();
	}
	@JsonSetter("pointFolderId")
	public void setPointFolderId(int pointFolderId){
		this.data.setPointFolderId(pointFolderId);
	}


}
