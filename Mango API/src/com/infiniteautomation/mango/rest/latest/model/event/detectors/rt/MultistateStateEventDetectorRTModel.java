/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import com.serotonin.m2m2.rt.event.detectors.MultistateStateDetectorRT;
import com.serotonin.m2m2.vo.event.detector.MultistateStateDetectorVO;

/**
 *
 * @author Terry Packer
 */
public class MultistateStateEventDetectorRTModel extends StateDetectorRTModel<MultistateStateDetectorVO> {

    public MultistateStateEventDetectorRTModel(MultistateStateDetectorRT rt) {
        super(rt);
    }

}
