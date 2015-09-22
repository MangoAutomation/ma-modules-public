/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mangoApi;

import java.io.File;
import java.net.MalformedURLException;

import org.eclipse.jetty.util.resource.Resource;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.module.ModuleElementDefinition;
import com.serotonin.m2m2.web.OverridingFileResource;
import com.serotonin.m2m2.web.mvc.rest.v1.serializers.MangoApiJsonModule;
import com.serotonin.m2m2.web.mvc.spring.MangoRestSpringConfiguration;

/**
 * @author Terry Packer
 *
 */
public class MangoApiModuleDefinition extends ModuleElementDefinition{
	
	 public static MangoApiReloadingProperties props;
	
	
	@Override
	public void preInitialize(){

		try {
			//Base Property File
			Resource base = Resource.newResource(Common.MA_HOME + getModule().getDirectoryPath() + File.separator + "classes" + File.separator + "mangoApiHeaders.properties");
			
			File overrideClasses = new File(Common.MA_HOME + File.separator + "overrides" + File.separator + "classes" + File.separator);
			
			//Overridden Property File
			Resource override = Resource.newResource(new File(overrideClasses, "mangoApiHeaders.properties"));
	        
			//We must have an overrides/classes folder for the Overrides to work properly even if the file isn't present.
			if(!overrideClasses.exists()){
				overrideClasses.mkdirs();
			}
			
			OverridingFileResource propertiesFile = new OverridingFileResource(override,base);
			props = new MangoApiReloadingProperties(propertiesFile);
		} catch (MalformedURLException e) {
			throw new ShouldNeverHappenException(e);
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.ModuleElementDefinition#postInitialize()
	 */
	@Override
	public void postInitialize() {
		super.postInitialize();
		
		//Hook into the Object Mapper 
		MangoRestSpringConfiguration.objectMapper.registerModule(new MangoApiJsonModule());
		
		
	}
}
