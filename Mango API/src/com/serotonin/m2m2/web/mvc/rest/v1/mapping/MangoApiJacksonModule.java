/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.mapping;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.serotonin.m2m2.web.mvc.rest.v1.model.JsonStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.ObjectStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryDataPageStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.email.EmailRecipientModel;

/**
 * @author Terry Packer
 *
 */
public class MangoApiJacksonModule extends SimpleModule {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MangoApiJacksonModule() {
		super("MangoApiJson", new Version(0, 0, 1, "SNAPSHOT", "com.infiniteautomation",
				"mango"));
		this.addSerializer(QueryArrayStream.class, new JsonArraySerializer());
		this.addSerializer(QueryDataPageStream.class, new JsonDataPageSerializer());
		this.addSerializer(ObjectStream.class, new JsonObjectSerializer());
        this.addSerializer(JsonStream.class, new JsonStreamSerializer());
		this.addDeserializer(EmailRecipientModel.class, new EmailRecipientModelDeserializer());

	}
	
	@Override
	public void setupModule(SetupContext context) {
		super.setupModule(context);
		
	}

}
