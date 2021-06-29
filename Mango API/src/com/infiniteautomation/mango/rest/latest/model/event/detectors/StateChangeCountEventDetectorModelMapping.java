/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.module.definitions.event.detectors.StateChangeCountEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.StateChangeCountDetectorVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

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
    public StateChangeCountEventDetectorModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        StateChangeCountDetectorVO detector = (StateChangeCountDetectorVO)from;
        return loadDataPoint(detector, new StateChangeCountEventDetectorModel(detector), user, mapper);
    }
    @Override
    public String getTypeName() {
        return StateChangeCountEventDetectorDefinition.TYPE_NAME;
    }
}
