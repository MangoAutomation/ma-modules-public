/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapping;
import com.serotonin.m2m2.rt.event.detectors.SmoothnessDetectorRT;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class SmoothnessEventDetectorRTModelMapping implements RestModelMapping<SmoothnessDetectorRT, SmoothnessEventDetectorRTModel> {

    @Override
    public Class<? extends SmoothnessDetectorRT> fromClass() {
        return SmoothnessDetectorRT.class;
    }

    @Override
    public Class<? extends SmoothnessEventDetectorRTModel> toClass() {
        return SmoothnessEventDetectorRTModel.class;
    }

    @Override
    public SmoothnessEventDetectorRTModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new SmoothnessEventDetectorRTModel((SmoothnessDetectorRT)from);
    }

}
