/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.module.definitions.event.detectors.UpdateEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.UpdateDetectorVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Jared Wiltshire
 */
@Component
public class UpdateEventDetectorModelMapping extends AbstractPointEventDetectorModelMapping<UpdateDetectorVO, UpdateEventDetectorModel> {

    @Override
    public Class<? extends UpdateDetectorVO> fromClass() {
        return UpdateDetectorVO.class;
    }

    @Override
    public Class<? extends UpdateEventDetectorModel> toClass() {
        return UpdateEventDetectorModel.class;
    }

    @Override
    public UpdateEventDetectorModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        UpdateDetectorVO detector = (UpdateDetectorVO) from;
        return loadDataPoint(detector, new UpdateEventDetectorModel(detector), user, mapper);
    }

    @Override
    public String getTypeName() {
        return UpdateEventDetectorDefinition.TYPE_NAME;
    }
}
