/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors.rt;

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
