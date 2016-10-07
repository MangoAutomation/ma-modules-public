/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.watchlist;

import java.util.Map;

/**
 * @author Terry Packer
 *
 */
public class WatchListParameter {
	
	private String name;
	private String type;
	private String label;
	private Map<String, Object> options;
	
	public WatchListParameter(){ }
	
	public WatchListParameter(String name, String type, String label, Map<String, Object> options) {
		super();
		this.name = name;
		this.type = type;
		this.label = label;
		this.options = options;
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
    public Map<String, Object> getOptions() {
        return options;
    }
    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }
}
