/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapping;
import com.serotonin.m2m2.rt.event.detectors.RateOfChangeDetectorRT;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class RateOfChangeEventDetectorRTModelMapping implements RestModelMapping<RateOfChangeDetectorRT, RateOfChangeEventDetectorRTModel> {

    @Override
    public Class<? extends RateOfChangeDetectorRT> fromClass() {
        return RateOfChangeDetectorRT.class;
    }

    @Override
    public Class<? extends RateOfChangeEventDetectorRTModel> toClass() {
        return RateOfChangeEventDetectorRTModel.class;
    }

    @Override
    public RateOfChangeEventDetectorRTModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new RateOfChangeEventDetectorRTModel((RateOfChangeDetectorRT)from);
    }

}
