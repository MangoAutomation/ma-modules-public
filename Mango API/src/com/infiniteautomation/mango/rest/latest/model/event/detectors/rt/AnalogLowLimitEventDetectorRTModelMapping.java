/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapping;
import com.serotonin.m2m2.rt.event.detectors.AnalogHighLimitDetectorRT;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class AnalogLowLimitEventDetectorRTModelMapping implements RestModelMapping<AnalogHighLimitDetectorRT, AnalogHighLimitEventDetectorRTModel> {

    @Override
    public Class<? extends AnalogHighLimitDetectorRT> fromClass() {
        return AnalogHighLimitDetectorRT.class;
    }

    @Override
    public Class<? extends AnalogHighLimitEventDetectorRTModel> toClass() {
        return AnalogHighLimitEventDetectorRTModel.class;
    }

    @Override
    public AnalogHighLimitEventDetectorRTModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new AnalogHighLimitEventDetectorRTModel((AnalogHighLimitDetectorRT)from);
    }

}
