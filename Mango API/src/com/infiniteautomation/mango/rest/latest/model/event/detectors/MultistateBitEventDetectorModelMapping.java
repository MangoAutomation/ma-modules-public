/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.module.definitions.event.detectors.MultistateBitEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.MultistateBitDetectorVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Jared Wiltshire
 */
@Component
public class MultistateBitEventDetectorModelMapping extends AbstractPointEventDetectorModelMapping<MultistateBitDetectorVO, MultistateBitEventDetectorModel> {

    @Override
    public Class<? extends MultistateBitDetectorVO> fromClass() {
        return MultistateBitDetectorVO.class;
    }

    @Override
    public Class<? extends MultistateBitEventDetectorModel> toClass() {
        return MultistateBitEventDetectorModel.class;
    }

    @Override
    public MultistateBitEventDetectorModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        MultistateBitDetectorVO detector = (MultistateBitDetectorVO) from;
        return loadDataPoint(detector, new MultistateBitEventDetectorModel(detector), user, mapper);
    }

    @Override
    public String getTypeName() {
        return MultistateBitEventDetectorDefinition.TYPE_NAME;
    }
}
