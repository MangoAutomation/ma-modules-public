/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.system;

/**
 * 
 * @author Terry Packer
 */
public class DiskInfoModel {
	
	private String name;
	private long totalSpaceBytes;
	private long usableSpaceBytes;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getTotalSpaceBytes() {
		return totalSpaceBytes;
	}
	public void setTotalSpaceBytes(long totalSpace) {
		this.totalSpaceBytes = totalSpace;
	}
	public long getUsableSpaceBytes() {
		return usableSpaceBytes;
	}
	public void setUsableSpaceBytes(long usableSpace) {
		this.usableSpaceBytes = usableSpace;
	}
}
