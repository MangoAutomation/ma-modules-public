/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.mbus.rest;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.serotonin.m2m2.module.JsonRestJacksonModuleDefinition;

/**
 * 
 * @author Terry Packer
 */
public class MBusJacksonModuleDefinition extends JsonRestJacksonModuleDefinition{

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.JsonRestJacksonModuleDefinition#getJacksonModule()
	 */
	@Override
	public SimpleModule getJacksonModule() {
		return new MBusJacksonModule();
	}

}
