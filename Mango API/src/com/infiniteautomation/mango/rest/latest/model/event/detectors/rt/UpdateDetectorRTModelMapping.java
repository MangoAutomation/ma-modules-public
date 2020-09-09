/*
 * Copyright (C) 2020 Infinite Automation Systems Inc. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapping;
import com.serotonin.m2m2.rt.event.detectors.UpdateDetectorRT;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Jared Wiltshire
 */
@Component
public class UpdateDetectorRTModelMapping implements RestModelMapping<UpdateDetectorRT, UpdateEventDetectorRTModel> {

    @Override
    public Class<? extends UpdateDetectorRT> fromClass() {
        return UpdateDetectorRT.class;
    }

    @Override
    public Class<? extends UpdateEventDetectorRTModel> toClass() {
        return UpdateEventDetectorRTModel.class;
    }

    @Override
    public UpdateEventDetectorRTModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new UpdateEventDetectorRTModel((UpdateDetectorRT) from);
    }

}
