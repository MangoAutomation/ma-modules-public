/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import com.serotonin.m2m2.rt.event.detectors.AlphanumericStateDetectorRT;
import com.serotonin.m2m2.vo.event.detector.AlphanumericStateDetectorVO;

/**
 *
 * @author Terry Packer
 */
public class AlphanumericStateEventDetectorRTModel extends StateDetectorRTModel<AlphanumericStateDetectorVO>{

    public AlphanumericStateEventDetectorRTModel(AlphanumericStateDetectorRT rt) {
        super(rt);
    }

}
