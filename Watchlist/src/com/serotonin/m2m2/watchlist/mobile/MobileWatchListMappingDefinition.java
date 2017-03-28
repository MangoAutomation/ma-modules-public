/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.watchlist.mobile;

import com.serotonin.m2m2.module.UriMappingDefinition;
import com.serotonin.m2m2.web.mvc.UrlHandler;

public class MobileWatchListMappingDefinition extends UriMappingDefinition {

    @Override
    public UrlHandler getHandler() {
        return new MobileWatchListHandler();
    }

    @Override
    public String getJspPath() {
        return "web/mobile/watchList.jsp";
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
		return "/mobile/watch_list.shtm";
	}
}
