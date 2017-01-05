/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.mapping;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.infiniteautomation.mango.monitor.AtomicIntegerMonitor;
import com.infiniteautomation.mango.monitor.DoubleMonitor;
import com.infiniteautomation.mango.monitor.FloatMonitor;
import com.infiniteautomation.mango.monitor.IntegerMonitor;
import com.infiniteautomation.mango.monitor.LongMonitor;
import com.infiniteautomation.mango.monitor.ObjectMonitor;
import com.infiniteautomation.mango.monitor.ValueMonitor;
import com.serotonin.m2m2.Common;

/**
 * 
 * @author Terry Packer
 */
public class ValueMonitorSerializer<T> extends JsonSerializer<ValueMonitor<T>>{

	private static final String ID = "id";	
	private static final String NAME = "name";
	private static final String VALUE = "value";
	private static final String TYPE = "modelType";
	
	/* (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
	 */
	@Override
	public void serialize(ValueMonitor<T> value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		
		jgen.writeStartObject();
		jgen.writeStringField(ID, value.getId());
		jgen.writeStringField(NAME, value.getName().translate(Common.getTranslations()));
		if(value instanceof AtomicIntegerMonitor){
			jgen.writeNumberField(VALUE, ((AtomicIntegerMonitor)value).getValue());
		}else if (value instanceof DoubleMonitor){
			jgen.writeNumberField(VALUE, ((DoubleMonitor)value).getValue());
		}else if (value instanceof FloatMonitor){
			jgen.writeNumberField(VALUE, ((FloatMonitor)value).getValue());
		}else if (value instanceof IntegerMonitor){
			jgen.writeNumberField(VALUE, ((IntegerMonitor)value).getValue());
		}else if (value instanceof LongMonitor){
			jgen.writeNumberField(VALUE, ((LongMonitor)value).getValue());
		}else if (value instanceof ObjectMonitor){
			jgen.writeObjectField(VALUE, ((ObjectMonitor<T>)value).getValue());
		}
		jgen.writeStringField(TYPE, value.getClass().getSimpleName());
		jgen.writeEndObject();
		
	}

}
