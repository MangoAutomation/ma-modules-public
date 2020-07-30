/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model.event.detectors.rt;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.serotonin.m2m2.rt.event.detectors.MultistateStateDetectorRT;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 *
 * @author Terry Packer
 */
@Component
public class MultistateStateEventDetectorRTModelMapping implements RestModelMapping<MultistateStateDetectorRT, MultistateStateEventDetectorRTModel> {

    @Override
    public Class<? extends MultistateStateDetectorRT> fromClass() {
        return MultistateStateDetectorRT.class;
    }

    @Override
    public Class<? extends MultistateStateEventDetectorRTModel> toClass() {
        return MultistateStateEventDetectorRTModel.class;
    }

    @Override
    public MultistateStateEventDetectorRTModel map(Object from, PermissionHolder user,
            RestModelMapper mapper) {
        return new MultistateStateEventDetectorRTModel((MultistateStateDetectorRT)from);
    }

}
