/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.serializers;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.serotonin.m2m2.web.mvc.rest.v1.model.ObjectStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryDataPageStream;

/**
 * @author Terry Packer
 *
 */
public class MangoApiJsonModule extends SimpleModule {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MangoApiJsonModule() {
		super("MangoApiJson", new Version(0, 0, 1, "SNAPSHOT", "com.infiniteautomation",
				"mango"));
		this.addSerializer(QueryArrayStream.class, new JsonArraySerializer());
		this.addSerializer(QueryDataPageStream.class, new JsonDataPageSerializer());
		this.addSerializer(ObjectStream.class, new JsonObjectSerializer());
	}
	
	@Override
	public void setupModule(SetupContext context) {
		super.setupModule(context);
		
	}

}
