/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import com.serotonin.m2m2.rt.event.detectors.MultistateBitDetectorRT;
import com.serotonin.m2m2.vo.event.detector.MultistateBitDetectorVO;

/**
 * @author Jared Wiltshire
 */
public class MultistateBitEventDetectorRTModel extends StateDetectorRTModel<MultistateBitDetectorVO> {

    public MultistateBitEventDetectorRTModel(MultistateBitDetectorRT rt) {
        super(rt);
    }

}
