/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.pointLinks;

import com.serotonin.m2m2.rt.dataImage.SetPointSource;

/**
 * @author Terry Packer
 *
 */
public interface PointLinkSetPointSource extends SetPointSource{

	public void pointSetComplete();
	
}
