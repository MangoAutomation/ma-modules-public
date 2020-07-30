/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model.event.detectors.rt;

import com.serotonin.m2m2.rt.event.detectors.AbstractEventDetectorRT;
import com.serotonin.m2m2.vo.event.detector.AbstractEventDetectorVO;

/**
 *
 * @author Terry Packer
 */
public abstract class AbstractEventDetectorRTModel<T extends AbstractEventDetectorVO> {

    public AbstractEventDetectorRTModel(AbstractEventDetectorRT<T> rt) {

    }
}
