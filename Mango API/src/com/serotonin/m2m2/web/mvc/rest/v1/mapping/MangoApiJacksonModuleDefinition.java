/*
   Copyright (C) 2016 Infinite Automation Systems Inc. All rights reserved.
   @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.mapping;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.serotonin.m2m2.module.JacksonModuleDefinition;

/**
 * Module for REST jackson mapper
 * 
 * @author Terry Packer
 *
 */
public class MangoApiJacksonModuleDefinition extends JacksonModuleDefinition{

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.JsonRestJacksonModuleDefinition#getJacksonModule()
	 */
	@Override
	public SimpleModule getJacksonModule() {
		return new MangoApiJacksonModule();
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.JacksonModuleDefinition#getSourceMapperType()
	 */
	@Override
	public ObjectMapperSource getSourceMapperType() {
	    return ObjectMapperSource.REST;
	}

}
