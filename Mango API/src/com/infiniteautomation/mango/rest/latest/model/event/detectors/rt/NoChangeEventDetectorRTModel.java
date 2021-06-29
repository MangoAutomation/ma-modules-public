/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import com.serotonin.m2m2.rt.event.detectors.NoChangeDetectorRT;
import com.serotonin.m2m2.vo.event.detector.NoChangeDetectorVO;

/**
 *
 * @author Terry Packer
 */
public class NoChangeEventDetectorRTModel extends DifferenceDetectorRTModel<NoChangeDetectorVO> {

    public NoChangeEventDetectorRTModel(NoChangeDetectorRT rt) {
        super(rt);
    }

}
