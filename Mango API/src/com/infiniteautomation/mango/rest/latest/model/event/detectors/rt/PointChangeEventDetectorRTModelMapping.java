/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapping;
import com.serotonin.m2m2.rt.event.detectors.PointChangeDetectorRT;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 *
 * @author Terry Packer
 */
@Component
public class PointChangeEventDetectorRTModelMapping implements RestModelMapping<PointChangeDetectorRT, PointChangeEventDetectorRTModel> {

    @Override
    public Class<? extends PointChangeDetectorRT> fromClass() {
        return PointChangeDetectorRT.class;
    }

    @Override
    public Class<? extends PointChangeEventDetectorRTModel> toClass() {
        return PointChangeEventDetectorRTModel.class;
    }

    @Override
    public PointChangeEventDetectorRTModel map(Object from, PermissionHolder user,
            RestModelMapper mapper) {
        return new PointChangeEventDetectorRTModel((PointChangeDetectorRT)from);
    }

}
