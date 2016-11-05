/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * @author Terry Packer
 *
 */
public class PointValueTimeJsonWriter extends PointValueTimeWriter{

	protected JsonGenerator jgen;
	
	public PointValueTimeJsonWriter(String host, int port, JsonGenerator jgen, boolean useRendered, boolean unitConversion){
		super(host, port, useRendered, unitConversion);
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
	public void writePointValueTime(String string, long timestamp, String annotation) throws IOException{
		jgen.writeStartObject();
		jgen.writeStringField("annotation", annotation);
    	jgen.writeStringField("value", string);
    	jgen.writeNumberField("timestamp", timestamp);
    	jgen.writeEndObject();
	}
	
	@Override
	public void writePointValueTime(DataValue value, long timestamp,
			String annotation, DataPointVO vo) throws IOException {
		
		jgen.writeStartObject();
		
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
				case DataTypes.IMAGE:
					jgen.writeStringField("value",imageServletBuilder.buildAndExpand(timestamp, vo.getId()).toUri().toString());
				break;
			}
		}
		
    	jgen.writeNumberField("timestamp", timestamp);
		jgen.writeStringField("annotation", annotation);

    	jgen.writeEndObject();
	}
}
