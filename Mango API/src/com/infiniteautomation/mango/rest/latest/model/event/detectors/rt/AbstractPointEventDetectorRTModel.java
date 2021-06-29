/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import com.serotonin.m2m2.rt.event.detectors.PointEventDetectorRT;
import com.serotonin.m2m2.vo.event.detector.AbstractPointEventDetectorVO;

/**
 *
 * @author Terry Packer
 */
public abstract class AbstractPointEventDetectorRTModel<T extends AbstractPointEventDetectorVO> extends AbstractEventDetectorRTModel<T> {

    public AbstractPointEventDetectorRTModel(PointEventDetectorRT<T> rt) {
        super(rt);
    }

}
