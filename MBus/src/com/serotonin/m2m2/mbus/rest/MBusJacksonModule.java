/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.mbus.rest;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

import net.sf.mbus4j.Connection;

/**
 * 
 * @author Terry Packer
 */
public class MBusJacksonModule  extends SimpleModule {

	private static final long serialVersionUID = 1L;

	public MBusJacksonModule() {
		super("MBus", new Version(0, 0, 1, "SNAPSHOT", "com.infiniteautomation", "mango"));
		this.addSerializer(Connection.class, new MBusConnectionSerializer());
        this.addDeserializer(Connection.class, new MBusConnectionDeserializer());
	}
	
}
