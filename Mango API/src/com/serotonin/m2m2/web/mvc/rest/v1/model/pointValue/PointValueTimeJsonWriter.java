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
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * @author Terry Packer
 *
 */
public class PointValueTimeJsonWriter extends PointValueTimeWriter{
	private final Log LOG = LogFactory.getLog(PointValueTimeJsonWriter.class);

	protected JsonGenerator jgen;
	
	public PointValueTimeJsonWriter(JsonGenerator jgen, DataPointVO vo, boolean useRendered, boolean unitConversion){
		super(vo, useRendered, unitConversion);
		this.jgen = jgen;
	}
	
	@Override
	public void writePointValueTime(double value, long timestamp, String annotation) throws IOException{
		jgen.writeStartObject();
		jgen.writeStringField("annotation", annotation);
    	jgen.writeNumberField("value", value);
    	jgen.writeNumberField("timestamp", timestamp);
    	jgen.writeEndObject();
	}
	
	@Override
	public void writePointValueTime(int value, long timestamp, String annotation) throws IOException{
		jgen.writeStartObject();
		jgen.writeStringField("annotation", annotation);
    	jgen.writeNumberField("value", value);
    	jgen.writeNumberField("timestamp", timestamp);
    	jgen.writeEndObject();
	}
	
	@Override
	public void writePointValueTime(DataValue value, long timestamp,
			String annotation) throws IOException {
		
		jgen.writeStartObject();
		jgen.writeStringField("annotation", annotation);
		
		if(value == null){
			jgen.writeNullField("value");
		}else{
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
		}
		
    	jgen.writeNumberField("timestamp", timestamp);
    	jgen.writeEndObject();
	}
}
