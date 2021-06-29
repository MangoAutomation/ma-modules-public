/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import com.serotonin.m2m2.module.definitions.event.detectors.NoChangeEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.NoChangeDetectorVO;

/**
 * 
 * @author Terry Packer
 */
public class NoChangeEventDetectorModel extends TimeoutDetectorModel<NoChangeDetectorVO>{

	public NoChangeEventDetectorModel(NoChangeDetectorVO data) {
		fromVO(data);
	}

	public NoChangeEventDetectorModel() { }
	
    @Override
    public String getDetectorType() {
        return NoChangeEventDetectorDefinition.TYPE_NAME;
    }

}
