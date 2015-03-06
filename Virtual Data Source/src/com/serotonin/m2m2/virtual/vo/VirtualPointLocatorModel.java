/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.virtual.vo;

import com.serotonin.m2m2.vo.dataSource.PointLocatorVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel;

/**
 * @author Terry Packer
 *
 */
public class VirtualPointLocatorModel extends PointLocatorModel<VirtualPointLocatorVO>{

	/**
	 * @param data
	 */
	public VirtualPointLocatorModel(PointLocatorVO data) {
		super(data);
	}

	public VirtualPointLocatorModel() {
		super(new VirtualPointLocatorVO());
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel#getTypeName()
	 */
	@Override
	public String getTypeName() {
		return VirtualPointLocatorModelDefinition.TYPE_NAME;
	}


	
}
