/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.pointLinks;

import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.maint.work.SetPointWorkItem;

/**
 * @author Terry Packer
 *
 */
public class PointLinkSetPointWorkItem extends SetPointWorkItem{

	private PointLinkSetPointSource plSource;
	
	/**
	 * @param targetPointId
	 * @param pvt
	 * @param source
	 */
	public PointLinkSetPointWorkItem(int targetPointId, PointValueTime pvt,
			PointLinkSetPointSource plSource, SetPointSource source) {
		super(targetPointId, pvt, source);
		this.plSource = plSource;
	}
	
	
	@Override
    public void execute() {
		super.execute();
		plSource.pointSetComplete();
	}
	
	

}
