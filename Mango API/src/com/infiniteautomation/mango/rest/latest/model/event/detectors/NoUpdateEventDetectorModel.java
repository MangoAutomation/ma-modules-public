/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import com.serotonin.m2m2.module.definitions.event.detectors.NoUpdateEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.NoUpdateDetectorVO;

/**
 * 
 * @author Terry Packer
 */
public class NoUpdateEventDetectorModel extends TimeoutDetectorModel<NoUpdateDetectorVO>{

	public NoUpdateEventDetectorModel(NoUpdateDetectorVO data) {
		fromVO(data);
	}
	
	public NoUpdateEventDetectorModel() {  }

    @Override
    public String getDetectorType() {
        return NoUpdateEventDetectorDefinition.TYPE_NAME;
    }
	
}
