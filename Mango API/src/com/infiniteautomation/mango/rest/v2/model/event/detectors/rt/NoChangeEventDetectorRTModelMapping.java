/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model.event.detectors.rt;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.serotonin.m2m2.rt.event.detectors.NoChangeDetectorRT;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 *
 * @author Terry Packer
 */
@Component
public class NoChangeEventDetectorRTModelMapping implements RestModelMapping<NoChangeDetectorRT, NoChangeEventDetectorRTModel> {

    @Override
    public Class<? extends NoChangeDetectorRT> fromClass() {
        return NoChangeDetectorRT.class;
    }

    @Override
    public Class<? extends NoChangeEventDetectorRTModel> toClass() {
        return NoChangeEventDetectorRTModel.class;
    }

    @Override
    public NoChangeEventDetectorRTModel map(Object from, PermissionHolder user,
            RestModelMapper mapper) {
        return new NoChangeEventDetectorRTModel((NoChangeDetectorRT)from);
    }

}
