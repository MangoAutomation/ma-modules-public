/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.mbus.rest;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import net.sf.mbus4j.Connection;
import net.sf.mbus4j.SerialPortConnection;
import net.sf.mbus4j.TcpIpConnection;

/**
 * 
 * @author Terry Packer
 */
public class MBusConnectionDeserializer extends StdDeserializer<Connection>{

	
	private static final long serialVersionUID = 1L;

	public MBusConnectionDeserializer() {
		super(Connection.class);
	}

	/* (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
	 */
	@Override
	public Connection deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {

		JsonNode n = p.readValueAsTree();
		JsonNode t = n.get("modelType");

		int bitPerSecond = n.get("bitPerSecond").asInt();
		int responseTimeoutOffset = n.get("responseTimeoutOffset").asInt();
		String type = null;
		
		if(t != null)
			type = t.asText();
		else
			throw new IOException("No connection model type provided.");
		
		switch(type){
		case "mbusSerial":
			SerialPortConnection serialModel = new SerialPortConnection(n.get("portName").asText(), bitPerSecond, responseTimeoutOffset);
			return serialModel;
		case "mbusTcpIp":
			TcpIpConnection tcpModel = new TcpIpConnection(n.get("host").asText(), n.get("port").asInt(), bitPerSecond, responseTimeoutOffset);
			return tcpModel;
		default:
			throw new IOException("Invalid model type: " + type);
		}
	}
}
