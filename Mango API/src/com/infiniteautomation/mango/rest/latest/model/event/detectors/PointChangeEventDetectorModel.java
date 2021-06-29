/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import com.serotonin.m2m2.module.definitions.event.detectors.PointChangeEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.PointChangeDetectorVO;

/**
 * 
 * @author Terry Packer
 */
public class PointChangeEventDetectorModel extends AbstractPointEventDetectorModel<PointChangeDetectorVO>{

	public PointChangeEventDetectorModel(PointChangeDetectorVO data) {
		fromVO(data);
	}

	public PointChangeEventDetectorModel() { }
	
    @Override
    public String getDetectorType() {
        return PointChangeEventDetectorDefinition.TYPE_NAME;
    }
	
}
