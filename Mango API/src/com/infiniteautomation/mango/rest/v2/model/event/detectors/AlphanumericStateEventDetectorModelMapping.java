/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.detector.AlphanumericStateDetectorVO;

/**
 * @author Terry Packer
 *
 */
@Component
public class AlphanumericStateEventDetectorModelMapping extends AbstractPointEventDetectorModelMapping<AlphanumericStateDetectorVO, AlphanumericStateEventDetectorModel> {

    @Override
    public Class<? extends AlphanumericStateDetectorVO> fromClass() {
        return AlphanumericStateDetectorVO.class;
    }

    @Override
    public Class<? extends AlphanumericStateEventDetectorModel> toClass() {
        return AlphanumericStateEventDetectorModel.class;
    }

    @Override
    public AlphanumericStateEventDetectorModel map(Object from, User user, RestModelMapper mapper) {
        AlphanumericStateDetectorVO detector = (AlphanumericStateDetectorVO)from;
        return loadDataPoint(detector, new AlphanumericStateEventDetectorModel(detector), user, mapper);
    }

}
