/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model.event.detectors.rt;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.serotonin.m2m2.rt.event.detectors.NoUpdateDetectorRT;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 *
 * @author Terry Packer
 */
@Component
public class NoUpdateEventDetectorRTModelMapping implements RestModelMapping<NoUpdateDetectorRT, NoUpdateEventDetectorRTModel> {

    @Override
    public Class<? extends NoUpdateDetectorRT> fromClass() {
        return NoUpdateDetectorRT.class;
    }

    @Override
    public Class<? extends NoUpdateEventDetectorRTModel> toClass() {
        return NoUpdateEventDetectorRTModel.class;
    }

    @Override
    public NoUpdateEventDetectorRTModel map(Object from, PermissionHolder user,
            RestModelMapper mapper) {
        return new NoUpdateEventDetectorRTModel((NoUpdateDetectorRT)from);
    }

}
