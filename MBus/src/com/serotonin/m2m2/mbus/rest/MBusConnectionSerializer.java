/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.mbus.rest;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import net.sf.mbus4j.Connection;
import net.sf.mbus4j.SerialPortConnection;
import net.sf.mbus4j.TcpIpConnection;

/**
 * 
 * @author Terry Packer
 */
public class MBusConnectionSerializer extends JsonSerializer<Connection>{

	/* (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
	 */
	@Override
	public void serialize(Connection value, JsonGenerator gen, SerializerProvider serializers)
			throws IOException, JsonProcessingException {
		
		gen.writeStartObject();
		gen.writeNumberField("bitPerSecond", value.getBitPerSecond());
		gen.writeNumberField("responseTimeoutOffset", value.getResponseTimeOutOffset());
		
		if(value instanceof TcpIpConnection){
			TcpIpConnection conn = (TcpIpConnection)value;
			gen.writeStringField("host", conn.getHost());
			gen.writeNumberField("port", conn.getPort());
			gen.writeStringField("modelType", "mbusTcpIp");
		}else if(value instanceof SerialPortConnection){
			SerialPortConnection conn = (SerialPortConnection)value;
			gen.writeStringField("portName", conn.getPortName());
			gen.writeStringField("modelType", "mbusSerial");
		}
		
		
		gen.writeEndObject();
		
	}

}
