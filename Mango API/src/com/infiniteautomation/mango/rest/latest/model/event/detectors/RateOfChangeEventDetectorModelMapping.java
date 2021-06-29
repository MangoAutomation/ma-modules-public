/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.module.definitions.event.detectors.RateOfChangeDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.RateOfChangeDetectorVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class RateOfChangeEventDetectorModelMapping extends AbstractPointEventDetectorModelMapping<RateOfChangeDetectorVO, RateOfChangeEventDetectorModel> {

    @Override
    public Class<? extends RateOfChangeDetectorVO> fromClass() {
        return RateOfChangeDetectorVO.class;
    }

    @Override
    public Class<? extends RateOfChangeEventDetectorModel> toClass() {
        return RateOfChangeEventDetectorModel.class;
    }

    @Override
    public RateOfChangeEventDetectorModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        RateOfChangeDetectorVO detector = (RateOfChangeDetectorVO)from;
        return loadDataPoint(detector, new RateOfChangeEventDetectorModel(detector), user, mapper);
    }

    @Override
    public String getTypeName() {
        return RateOfChangeDetectorDefinition.TYPE_NAME;
    }
}
