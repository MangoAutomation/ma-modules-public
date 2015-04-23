/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews;

import com.serotonin.m2m2.module.UriMappingDefinition;
import com.serotonin.m2m2.web.mvc.UrlHandler;

public class GraphicalViewsMappingDefinition extends UriMappingDefinition {
	
    @Override
    public UrlHandler getHandler() {
        return new GraphicalViewHandler();
    }

    @Override
    public String getJspPath() {
        return "web/views.jsp";
    }

    @Override
    public Permission getPermission() {
        return Permission.USER;
    }
    
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.UriMappingDefinition#getPath()
	 */
	@Override
	public String getPath() {
		return "/views.shtm";
	}

	
	
	
	
	
	
	
}
