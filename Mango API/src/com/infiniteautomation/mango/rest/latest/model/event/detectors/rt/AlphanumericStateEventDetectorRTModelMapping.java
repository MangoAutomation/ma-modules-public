/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors.rt;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.serotonin.m2m2.rt.event.detectors.AlphanumericStateDetectorRT;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class AlphanumericStateEventDetectorRTModelMapping implements RestModelMapping<AlphanumericStateDetectorRT, AlphanumericStateEventDetectorRTModel> {

    @Override
    public Class<? extends AlphanumericStateDetectorRT> fromClass() {
        return AlphanumericStateDetectorRT.class;
    }

    @Override
    public Class<? extends AlphanumericStateEventDetectorRTModel> toClass() {
        return AlphanumericStateEventDetectorRTModel.class;
    }

    @Override
    public AlphanumericStateEventDetectorRTModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new AlphanumericStateEventDetectorRTModel((AlphanumericStateDetectorRT)from);
    }

}
