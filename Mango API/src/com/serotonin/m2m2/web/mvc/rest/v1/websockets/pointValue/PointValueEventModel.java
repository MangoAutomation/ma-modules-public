/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.websockets.pointValue;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeModel;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author Terry Packer
 *
 */
public class PointValueEventModel {
    @JsonProperty("xid")
    private String xid;

	@JsonProperty("event")
	private PointValueEventType event;
	
	@JsonProperty("value")
	private PointValueTimeModel value;
	
	@JsonProperty("renderedValue")
	private String renderedValue;
	
	//The value converted 
	@JsonProperty("convertedValue")
	private Double convertedValue;
	
	@ApiModelProperty("Is the point running in the runtime")
	@JsonProperty("enabled")
	private boolean enabled;
	
	@ApiModelProperty("The state of the point's enabled property")
	@JsonProperty("pointEnabled")
	private boolean pointEnabled;
	
	@JsonProperty("attributes")
	private Map<String, Object> attributes;

	public PointValueEventModel(String xid, boolean enabled, boolean pointEnabled, Map<String,Object> attributes, PointValueEventType type, PointValueTimeModel model, String renderedValue, Double convertedValue){
	    this.xid = xid;
		this.event = type;
		this.value = model;
		this.enabled = enabled;
		this.pointEnabled = pointEnabled;
		this.attributes = attributes;
		this.renderedValue = renderedValue;
		this.convertedValue = convertedValue;
	}

	public PointValueEventModel(){
		this.attributes = new HashMap<String,Object>();
	}
	
	public PointValueEventType getEvent() {
		return event;
	}

	public void setEvent(PointValueEventType event) {
		this.event = event;
	}

	public PointValueTimeModel getValue() {
		return value;
	}

	public void setValue(PointValueTimeModel value) {
		this.value = value;
	}

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
    public boolean isPointEnabled() {
        return pointEnabled;
    }
    
    public void setPointEnabled(boolean pointEnabled) {
        this.pointEnabled = pointEnabled;
    }

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public String getRenderedValue() {
		return renderedValue;
	}

	public void setRenderedValue(String renderedValue) {
		this.renderedValue = renderedValue;
	}

	public Double getConvertedValue() {
		return convertedValue;
	}

	public void setConvertedValue(Double convertedValue) {
		this.convertedValue = convertedValue;
	}
    
    
}
