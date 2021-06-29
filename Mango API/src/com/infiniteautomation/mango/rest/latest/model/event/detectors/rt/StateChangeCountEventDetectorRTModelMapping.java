/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapping;
import com.serotonin.m2m2.rt.event.detectors.StateChangeCountDetectorRT;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class StateChangeCountEventDetectorRTModelMapping implements RestModelMapping<StateChangeCountDetectorRT, StateChangeCountEventDetectorRTModel> {

    @Override
    public Class<? extends StateChangeCountDetectorRT> fromClass() {
        return StateChangeCountDetectorRT.class;
    }

    @Override
    public Class<? extends StateChangeCountEventDetectorRTModel> toClass() {
        return StateChangeCountEventDetectorRTModel.class;
    }

    @Override
    public StateChangeCountEventDetectorRTModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new StateChangeCountEventDetectorRTModel((StateChangeCountDetectorRT)from);
    }

}
