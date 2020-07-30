/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import com.serotonin.m2m2.rt.event.detectors.NoUpdateDetectorRT;
import com.serotonin.m2m2.vo.event.detector.NoUpdateDetectorVO;

/**
 *
 * @author Terry Packer
 */
public class NoUpdateEventDetectorRTModel extends DifferenceDetectorRTModel<NoUpdateDetectorVO> {

    public NoUpdateEventDetectorRTModel(NoUpdateDetectorRT rt) {
        super(rt);
    }

}
