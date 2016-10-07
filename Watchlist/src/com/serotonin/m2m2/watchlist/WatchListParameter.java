/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.watchlist;

/**
 * @author Terry Packer
 *
 */
public class WatchListParameter {
	
	private String name;
	private String type;
	private String label;
	
	public WatchListParameter(){ }
	
	public WatchListParameter(String name, String type, String label) {
		super();
		this.name = name;
		this.type = type;
		this.label = label;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	
	
}
