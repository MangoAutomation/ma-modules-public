/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.virtual.vo;

import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel;

/**
 * TODO this class should be removed and we should create
 * models for all the different types of VOs in the Point Locator.
 * 
 * Then the definitions can be matched on type such as:
 * 
 * PL.VIRTUAL.AlternateBooleanChange
 * PL.VIRTUAL.AnalogAttractorChange
 * PL.VIRTUAL.BrownianChange 
 * etc.
 * 
 * This will greatly simplify the Models and be much clearer on what the point is
 * 
 * 
 * @author Terry Packer
 *
 */
public class VirtualPointLocatorModel extends PointLocatorModel<VirtualPointLocatorVO>{

	/**
	 * @param data
	 */
	public VirtualPointLocatorModel(VirtualPointLocatorVO data) {
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
