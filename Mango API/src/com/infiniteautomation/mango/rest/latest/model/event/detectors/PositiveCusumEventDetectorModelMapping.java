/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.module.definitions.event.detectors.PositiveCusumEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.PositiveCusumDetectorVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class PositiveCusumEventDetectorModelMapping extends AbstractPointEventDetectorModelMapping<PositiveCusumDetectorVO, PositiveCusumEventDetectorModel> {

    @Override
    public Class<? extends PositiveCusumDetectorVO> fromClass() {
        return PositiveCusumDetectorVO.class;
    }

    @Override
    public Class<? extends PositiveCusumEventDetectorModel> toClass() {
        return PositiveCusumEventDetectorModel.class;
    }

    @Override
    public PositiveCusumEventDetectorModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        PositiveCusumDetectorVO detector = (PositiveCusumDetectorVO)from;
        return loadDataPoint(detector, new PositiveCusumEventDetectorModel(detector), user, mapper);
    }
    @Override
    public String getTypeName() {
        return PositiveCusumEventDetectorDefinition.TYPE_NAME;
    }
}
