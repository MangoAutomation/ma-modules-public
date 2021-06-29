/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapping;
import com.serotonin.m2m2.rt.event.detectors.MultistateBitDetectorRT;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Jared Wiltshire
 */
@Component
public class MultistateBitEventDetectorRTModelMapping implements RestModelMapping<MultistateBitDetectorRT, MultistateBitEventDetectorRTModel> {

    @Override
    public Class<? extends MultistateBitDetectorRT> fromClass() {
        return MultistateBitDetectorRT.class;
    }

    @Override
    public Class<? extends MultistateBitEventDetectorRTModel> toClass() {
        return MultistateBitEventDetectorRTModel.class;
    }

    @Override
    public MultistateBitEventDetectorRTModel map(Object from, PermissionHolder user,
            RestModelMapper mapper) {
        return new MultistateBitEventDetectorRTModel((MultistateBitDetectorRT)from);
    }

}
