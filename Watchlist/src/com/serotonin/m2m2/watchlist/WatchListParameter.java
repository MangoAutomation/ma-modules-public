/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.watchlist;

import java.io.IOException;
import java.util.Map;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;

/**
 * @author Terry Packer
 *
 */
public class WatchListParameter implements JsonSerializable{
	
	@JsonProperty
	private String name;
	@JsonProperty
	private String type;
	@JsonProperty
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

	/* (non-Javadoc)
	 * @see com.serotonin.json.spi.JsonSerializable#jsonWrite(com.serotonin.json.ObjectWriter)
	 */
	@Override
	public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
		writer.writeEntry("options", this.options);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.json.spi.JsonSerializable#jsonRead(com.serotonin.json.JsonReader, com.serotonin.json.type.JsonObject)
	 */
	@Override
	public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
		JsonObject o = jsonObject.getJsonObject("options");
		if(o != null)
			this.options = o.toMap();
	}
}
