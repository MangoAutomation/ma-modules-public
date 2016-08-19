/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.jviews;

import com.serotonin.m2m2.module.UriMappingDefinition;
import com.serotonin.m2m2.web.mvc.UrlHandler;

/**
 * @author Terry Packer
 *
 */
public class JspViewsUriMappingDefinition extends UriMappingDefinition{

	public static final String urlBase = "/jsp-views/";
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.UriMappingDefinition#getPermission()
	 */
	@Override
	public Permission getPermission() {
		return Permission.USER;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.UriMappingDefinition#getPath()
	 */
	@Override
	public String getPath() {
		return urlBase + "**.shtm";
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.UriMappingDefinition#getHandler()
	 */
	@Override
	public UrlHandler getHandler() {
		return new JspViewsUrlHandler(getModule().getDirectoryPath(), getModule().getWebPath());
	}
 
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.UriMappingDefinition#getJspPath()
	 */
	@Override
	public String getJspPath() {
		return null; //Not sure of the best way to do this, we don't every use the jsp path
	}

}
