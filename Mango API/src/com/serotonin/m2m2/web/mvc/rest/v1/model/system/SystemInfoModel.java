/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.system;

import java.util.List;

import com.serotonin.m2m2.vo.bean.PointHistoryCount;

/**
 * 
 * @author Terry Packer
 */
public class SystemInfoModel {
	
	public SystemInfoModel(){ }
	
	private Long sqlDbSizeBytes;
	private Long noSqlDbSizeBytes;
	private Long fileDataSizeBytes;
	private List<PointHistoryCount> topPoints;
	private Integer eventCount;
	private List<DiskInfoModel> disks;
	private double loadAverage;
	private String architecture;
	private String operatingSystem;
	private String osVersion;
	
	public Long getSqlDbSizeBytes() {
		return sqlDbSizeBytes;
	}
	public void setSqlDbSizeBytes(Long sqlDbSizeBytes) {
		this.sqlDbSizeBytes = sqlDbSizeBytes;
	}
	public Long getNoSqlDbSizeBytes() {
		return noSqlDbSizeBytes;
	}
	public void setNoSqlDbSizeBytes(Long noSqlDbSizeBytes) {
		this.noSqlDbSizeBytes = noSqlDbSizeBytes;
	}
	public Long getFileDataSizeBytes() {
		return fileDataSizeBytes;
	}
	public void setFileDataSizeBytes(Long fileDataSizeBytes) {
		this.fileDataSizeBytes = fileDataSizeBytes;
	}
	public List<PointHistoryCount> getTopPoints() {
		return topPoints;
	}
	public void setTopPoints(List<PointHistoryCount> topPoints) {
		this.topPoints = topPoints;
	}
	public Integer getEventCount() {
		return eventCount;
	}
	public void setEventCount(Integer eventCount) {
		this.eventCount = eventCount;
	}
	public List<DiskInfoModel> getDisks() {
		return disks;
	}
	public void setDisks(List<DiskInfoModel> disks) {
		this.disks = disks;
	}
	public double getLoadAverage() {
		return loadAverage;
	}
	public void setLoadAverage(double loadAverage) {
		this.loadAverage = loadAverage;
	}
	public String getArchitecture() {
		return architecture;
	}
	public void setArchitecture(String architecture) {
		this.architecture = architecture;
	}
	public String getOperatingSystem() {
		return operatingSystem;
	}
	public void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}
	public String getOsVersion() {
		return osVersion;
	}
	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}
	
	
}
