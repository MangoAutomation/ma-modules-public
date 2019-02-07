/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.detector.StateChangeCountDetectorVO;

/**
 * @author Terry Packer
 *
 */
@Component
public class StateChangeCountEventDetectorModelMapping extends AbstractPointEventDetectorModelMapping<StateChangeCountDetectorVO, StateChangeCountEventDetectorModel> {

    @Override
    public Class<? extends StateChangeCountDetectorVO> fromClass() {
        return StateChangeCountDetectorVO.class;
    }

    @Override
    public Class<? extends StateChangeCountEventDetectorModel> toClass() {
        return StateChangeCountEventDetectorModel.class;
    }

    @Override
    public StateChangeCountEventDetectorModel map(Object from, User user, RestModelMapper mapper) {
        StateChangeCountDetectorVO detector = (StateChangeCountDetectorVO)from;
        return loadDataPoint(detector, new StateChangeCountEventDetectorModel(detector), user, mapper);
    }

}
