/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.watchlist.WatchListDao;
import com.serotonin.m2m2.watchlist.WatchListVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.PageQueryStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryStreamCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.WatchListSummaryModel;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
public class WatchListPageQueryStream extends PageQueryStream<WatchListVO, WatchListSummaryModel, WatchListDao>{

	/**
	 * @param dao
	 * @param controller
	 * @param node
	 * @param queryCallback
	 */
	public WatchListPageQueryStream(
			MangoVoRestController<WatchListVO, WatchListSummaryModel, WatchListDao> controller, ASTNode query,
			QueryStreamCallback<WatchListVO> queryCallback) {
		super(WatchListDao.instance, controller, query, queryCallback);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.PageQueryStream#streamCount(com.fasterxml.jackson.core.JsonGenerator)
	 */
	@Override
	public void streamCount(JsonGenerator jgen) throws IOException {
		jgen.writeNumber(this.queryCallback.getWrittenCount());
	}
	
}
