/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint;

import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController;
import com.serotonin.m2m2.web.mvc.rest.v1.model.DataPointModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.FilteredVoStreamCallback;

/**
 * Class to discard any data points that the user does not have access to during a query response.
 * 
 * @author Terry Packer
 *
 */
public class DataPointStreamCallback extends FilteredVoStreamCallback<DataPointVO, DataPointModel, DataPointDao>{

	private final DataPointFilter filter;
	
	/**
	 * @param controller
	 */
	public DataPointStreamCallback(
			MangoVoRestController<DataPointVO, DataPointModel, DataPointDao> controller,
			User user) {
		super(controller);
		this.filter = new DataPointFilter(user);

	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.FilteredQueryStreamCallback#filter(java.lang.Object)
	 */
	@Override
	protected boolean filter(DataPointVO vo) {
		return !this.filter.hasDataPointReadPermission(vo);
	}
	
}
