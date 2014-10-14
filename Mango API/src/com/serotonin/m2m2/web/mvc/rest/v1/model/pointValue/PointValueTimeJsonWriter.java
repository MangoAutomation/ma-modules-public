/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;

/**
 * @author Terry Packer
 *
 */
public abstract class PointValueTimeJsonWriter {
	private final Log LOG = LogFactory.getLog(PointValueTimeJsonWriter.class);

	private JsonGenerator jgen;
	
	public PointValueTimeJsonWriter(JsonGenerator jgen){
		this.jgen = jgen;
	}
	
	protected void writePointValueTime(double value, long timestamp, String annotation) throws IOException{
		jgen.writeStartObject();
		jgen.writeStringField("annotation", annotation);
    	jgen.writeNumberField("value", value);
    	jgen.writeNumberField("timestamp", timestamp);
    	jgen.writeEndObject();
	}

	protected void writePointValueTime(int value, long timestamp, String annotation) throws IOException{
		jgen.writeStartObject();
		jgen.writeStringField("annotation", annotation);
    	jgen.writeNumberField("value", value);
    	jgen.writeNumberField("timestamp", timestamp);
    	jgen.writeEndObject();
	}
	
	protected void writePointValueTime(DataValue value, long timestamp,
			String annotation) throws IOException {
		
		jgen.writeStartObject();
		jgen.writeStringField("annotation", annotation);
		
		switch(value.getDataType()){
			case DataTypes.ALPHANUMERIC:
				jgen.writeStringField("value", value.getStringValue());
			break;
			case DataTypes.BINARY:
				jgen.writeBooleanField("value", value.getBooleanValue());
			break;
			case DataTypes.MULTISTATE:
				jgen.writeNumberField("value", value.getIntegerValue());
			break;
			case DataTypes.NUMERIC:
				jgen.writeNumberField("value", value.getDoubleValue());
			break;
			default:
				jgen.writeStringField("value","unsupported-value-type");
				LOG.error("Unsupported data type for Point Value Time: " + value.getDataType());
			break;
		}
		
    	jgen.writeNumberField("timestamp", timestamp);
    	jgen.writeEndObject();
	}
}
