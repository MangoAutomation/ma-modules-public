/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.serotonin.m2m2.web.mvc.rest.v1.model.JsonArrayStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.JsonObjectStream;

/**
 * @author Terry Packer
 *
 */
public class MangoApiJsonModule extends SimpleModule {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MangoApiJsonModule() {
		super("MangoApiJson", new Version(0, 0, 1, "SNAPSHOT", "com.infiniteautomation",
				"mango"));
		this.addSerializer(JsonArrayStream.class, new JsonArraySerializer());
		this.addSerializer(JsonObjectStream.class, new JsonObjectSerializer());
	}
	
	@Override
	public void setupModule(SetupContext context) {
		super.setupModule(context);
		
	}

}
