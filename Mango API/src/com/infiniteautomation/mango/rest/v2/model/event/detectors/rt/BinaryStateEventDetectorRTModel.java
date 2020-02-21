/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors.rt;

import com.serotonin.m2m2.rt.event.detectors.BinaryStateDetectorRT;
import com.serotonin.m2m2.vo.event.detector.BinaryStateDetectorVO;

/**
 *
 * @author Terry Packer
 */
public class BinaryStateEventDetectorRTModel extends StateDetectorRTModel<BinaryStateDetectorVO> {

    public BinaryStateEventDetectorRTModel(BinaryStateDetectorRT rt) {
        super(rt);
    }

}
