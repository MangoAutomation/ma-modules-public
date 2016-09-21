/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.watchlist.WatchListDao;
import com.serotonin.m2m2.watchlist.WatchListVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.FilteredVoStreamCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.WatchListSummaryModel;

/**
 * Overidden stream to allow filtering on read permissions
 * @author Terry Packer
 *
 */
public class WatchListStreamCallback extends FilteredVoStreamCallback<WatchListVO, WatchListSummaryModel, WatchListDao>{

	private User user;
	
	/**
	 * @param controller
	 */
	public WatchListStreamCallback(MangoVoRestController<WatchListVO, WatchListSummaryModel, WatchListDao> controller, User user) {
		super(controller);
		this.user = user;
	}

	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.VoStreamCallback#canWrite(java.lang.Object)
	 */
	@Override
	protected boolean filter(WatchListVO vo) {
		//Allow this watchlist to be written out to the user?
		return !WatchListRestController.hasReadPermission(user, vo);
	}
}
