/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.detector.MultistateStateDetectorVO;

/**
 * @author Terry Packer
 *
 */
@Component
public class MultistateStateEventDetectorModelMapping extends AbstractPointEventDetectorModelMapping<MultistateStateDetectorVO, MultistateStateEventDetectorModel> {

    @Override
    public Class<? extends MultistateStateDetectorVO> fromClass() {
        return MultistateStateDetectorVO.class;
    }

    @Override
    public Class<? extends MultistateStateEventDetectorModel> toClass() {
        return MultistateStateEventDetectorModel.class;
    }

    @Override
    public MultistateStateEventDetectorModel map(Object from, User user, RestModelMapper mapper) {
        MultistateStateDetectorVO detector = (MultistateStateDetectorVO)from;
        return loadDataPoint(detector, new MultistateStateEventDetectorModel(detector), user, mapper);
    }

}
